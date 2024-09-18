package org.datamigration.usecase;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.datamigration.cache.ProcessingScopeCache;
import org.datamigration.domain.exception.ProjectForbiddenException;
import org.datamigration.domain.model.ItemStatusModel;
import org.datamigration.domain.model.ScopeModel;
import org.datamigration.jpa.entity.ItemEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.logger.BatchProcessingLogger;
import org.datamigration.model.BatchProcessingModel;
import org.datamigration.service.AsyncBatchService;
import org.datamigration.usecase.model.ImportDataResponseModel;
import org.datamigration.utils.DataMigrationUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class ImportDataUsecase {

    @Value("${batch.size}")
    private int batchSizeEnv;
    @Value("${batch.threads}")
    private int batchThreadsEnv;

    @Value("${batch.retry.scope.max}")
    private int batchRetryScopeMax;
    @Value("${batch.retry.scope.delayMs}")
    private long batchRetryScopeDelayMs;

    @Value("${batch.retry.batch.max}")
    private int batchRetryBatchMax;

    @Value("${batch.waitForBatchesToFinish.delayMs}")
    private long batchWaitForBatchesToFinishDelayMs;

    private final S3Usecase s3Usecase;
    private final ProjectsUsecase projectsUsecase;
    private final ScopesUsecase scopesUsecase;
    private final CheckpointsUsecase checkpointsUsecase;
    private final AsyncBatchService asyncBatchService;
    private final ProcessingScopeCache processingScopeCache;

    private ExecutorService executorService;
    private AtomicLong activeBatches;

    @PostConstruct
    void configure() {
        executorService = new ThreadPoolExecutor(
                batchThreadsEnv,
                batchThreadsEnv,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(batchThreadsEnv),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        activeBatches = new AtomicLong(0);
    }

    public ImportDataResponseModel importFromFile(MultipartFile file, UUID projectId, String owner)
            throws ProjectForbiddenException {
        projectsUsecase.isPermitted(projectId, owner);
        final String fileName =
                FilenameUtils.getBaseName(file.getOriginalFilename()) + "-" + DataMigrationUtils.getTimeStamp() + "." +
                        FilenameUtils.getExtension(file.getOriginalFilename());
        final Callable<InputStream> inputStreamCallable = file::getInputStream;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            final long lineCount = reader.lines().count() - 1;
            return importData(inputStreamCallable, projectId, fileName, lineCount, false);
        } catch (IOException ex) {
            throw new IllegalStateException();
        }
    }

    public ImportDataResponseModel importFromS3(String bucket, String key, String owner) throws ProjectForbiddenException {
        s3Usecase.isPermitted(key, owner);
        final UUID projectId = DataMigrationUtils.getProjectIdFromS3Key(key);
        final String fileName = DataMigrationUtils.getFileNameFromS3Key(key);
        final Callable<InputStream> inputStreamCallable = () -> s3Usecase.getObject(bucket, key, owner);
        final long lineCount = Long.parseLong(s3Usecase.getObjectTag(bucket, key, owner, "lineCount"));
        final ImportDataResponseModel importDataResponse = importData(inputStreamCallable, projectId, fileName, lineCount, true);
        if (importDataResponse.isSuccess()) {
            s3Usecase.deleteObject(bucket, key, owner);
        }
        return importDataResponse;
    }

    private ImportDataResponseModel importData(Callable<InputStream> inputStreamCallable, UUID projectId, String fileName,
                                               long lineCount, boolean external) {
        if (!fileName.toLowerCase().endsWith("csv".toLowerCase())) {
            throw new RuntimeException();
        }

        long startTime = System.currentTimeMillis();

        boolean success = false;
        int attempt = 0;

        final ScopeModel scopeModel = projectsUsecase.addScope(projectId, fileName, external);
        final ScopeEntity scopeEntity = scopesUsecase.get(scopeModel.getId());

        if (!processingScopeCache.getProcessingScopes().add(scopeEntity.getId())) {
            BatchProcessingLogger.log(Level.WARN, fileName, scopeEntity.getId(),
                    "Scope is already being processed, skipping batch processing.");
            return ImportDataResponseModel.builder()
                    .skipped(true)
                    .success(false)
                    .build();
        }

        try {
            while (attempt < batchRetryScopeMax && !success) {
                attempt++;

                BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(),
                        "Starting attempt " + attempt + " of " + batchRetryScopeMax + ".");
                if (scopeEntity.isFinished()) {
                    BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(),
                            "Scope was already successfully processed, skipping batch processing.");
                    return ImportDataResponseModel.builder()
                            .skipped(true)
                            .success(true)
                            .build();
                }

                final int batchSize = checkpointsUsecase.createOrGetCheckpointBy(scopeEntity, lineCount, batchSizeEnv);

                success =
                        batchProcessing(inputStreamCallable, projectId, fileName, scopeEntity, batchSize, startTime, attempt);
            }

            if (!success) {
                BatchProcessingLogger.log(Level.ERROR, fileName, scopeEntity.getId(),
                        "All retries failed. Batch processing aborted.");
                if (!external) {
                    scopesUsecase.delete(scopeEntity.getId());
                }
            } else {
                checkpointsUsecase.deleteByScopeId(scopeEntity.getId());
            }

            return ImportDataResponseModel.builder()
                    .skipped(false)
                    .success(success)
                    .build();
        } catch (Exception ex) {
            BatchProcessingLogger.log(Level.ERROR, fileName, scopeEntity.getId(), "Error occurred: " + ex.getMessage());
            return ImportDataResponseModel.builder()
                    .skipped(false)
                    .success(false)
                    .build();
        } finally {
            processingScopeCache.getProcessingScopes().remove(scopeEntity.getId());
        }
    }

    private boolean batchProcessing(Callable<InputStream> inputStreamCallable, UUID projectId, String fileName,
                                    ScopeEntity scopeEntity, int batchSize, long startTime, int attempt) {
        try (InputStream inputStream = inputStreamCallable.call();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            final String[] headers = reader.readLine().split(",");

            final List<ItemEntity> batch = new ArrayList<>();
            final AtomicLong batchIndex = new AtomicLong(1);
            final AtomicLong lineCounter = new AtomicLong(-1);

            final AtomicLong activeBatchesScope = new AtomicLong(0);
            final AtomicBoolean failed = new AtomicBoolean(false);

            final AtomicBoolean batchAlreadyProcessedCache = new AtomicBoolean(false);

            BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(), "Starting to process.");
            reader.lines()
                    .takeWhile(line -> !failed.get())
                    .forEach(line -> {
                        batchIndex.set((lineCounter.incrementAndGet() / batchSize) + 1);

                        if (isBatchAlreadyProcessed(fileName, lineCounter, batchSize, scopeEntity, batchIndex,
                                batchAlreadyProcessedCache)) {
                            return;
                        }

                        final ItemEntity itemEntity = getItemEntity(line, scopeEntity, headers);
                        batch.add(itemEntity);

                        handleFullBatch(projectId, fileName, batch, batchSize, scopeEntity, batchIndex, failed,
                                activeBatches, activeBatchesScope);
                    });

            handleLastBatch(projectId, fileName, batch, scopeEntity, batchIndex, batchSize, failed, activeBatches,
                    activeBatchesScope);

            while (activeBatchesScope.get() > 0) {
                waitForRemainingBatchesToFinish(fileName, scopeEntity);
            }

            if (!failed.get()) {
                if (activeBatchesScope.get() <= 0) {
                    scopesUsecase.finish(scopeEntity.getId());
                    long estimatedTime = System.currentTimeMillis() - startTime;
                    BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(),
                            "All batches processed. Total time: " + estimatedTime + " ms.");
                    return true;
                }
            } else {
                BatchProcessingLogger.log(Level.ERROR, fileName, scopeEntity.getId(),
                        "Batch processing was interrupted due to a failure.");
            }

        } catch (Exception ex) {
            BatchProcessingLogger.log(Level.ERROR, fileName, scopeEntity.getId(),
                    "Attempt " + attempt + " failed: " + ex.getMessage());
            scopeRetryDelay(attempt);
        }
        return false;
    }

    private void scopeRetryDelay(int attempt) {
        if (attempt < batchRetryScopeMax) {
            try {
                Thread.sleep(batchRetryScopeDelayMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean isBatchAlreadyProcessed(String fileName, AtomicLong lineCounter, int batchSize, ScopeEntity scopeEntity,
                                            AtomicLong batchIndex, AtomicBoolean batchAlreadyProcessedCache) {
        if (lineCounter.get() % batchSize == 0) {
            if (checkpointsUsecase.isBatchAlreadyProcessed(scopeEntity.getId(), batchIndex.get())) {
                batchAlreadyProcessedCache.set(true);
                BatchProcessingLogger.log(Level.DEBUG, fileName, scopeEntity.getId(),
                        "Batch " + batchIndex.get() + " already processed, skipping batch.");
                return true;
            }
            batchAlreadyProcessedCache.set(false);
        }

        return batchAlreadyProcessedCache.get();
    }

    private void handleFullBatch(UUID projectId, String fileName, List<ItemEntity> batch, int batchSize, ScopeEntity scopeEntity,
                                 AtomicLong batchIndex, AtomicBoolean failed, AtomicLong activeBatches,
                                 AtomicLong activeBatchesScope) {
        if (batch.size() >= batchSize) {
            final BatchProcessingModel batchProcessing = BatchProcessingModel.builder()
                    .projectId(projectId)
                    .scopeId(scopeEntity.getId())
                    .fileName(fileName)
                    .batchIndex(batchIndex.get())
                    .batchSize(batchSize)
                    .batch(new ArrayList<>(batch))
                    .build();
            asyncBatchService.handleBatch(batchProcessing, failed, activeBatches, activeBatchesScope, batchRetryBatchMax,
                    executorService);
            batch.clear();
        }
    }

    private void handleLastBatch(UUID projectId, String fileName, List<ItemEntity> batch, ScopeEntity scopeEntity,
                                 AtomicLong batchIndex, int batchSize, AtomicBoolean failed, AtomicLong activeBatches,
                                 AtomicLong activeBatchesScope) {
        if (!batch.isEmpty()) {
            final BatchProcessingModel batchProcessing = BatchProcessingModel.builder()
                    .projectId(projectId)
                    .scopeId(scopeEntity.getId())
                    .fileName(fileName)
                    .batchIndex(batchIndex.get())
                    .batchSize(batchSize)
                    .batch(new ArrayList<>(batch))
                    .build();
            asyncBatchService.handleBatch(batchProcessing, failed, activeBatches, activeBatchesScope, batchRetryBatchMax,
                    executorService);
        }
    }

    private void waitForRemainingBatchesToFinish(String fileName, ScopeEntity scopeEntity) {
        try {
            BatchProcessingLogger.log(Level.TRACE, fileName, scopeEntity.getId(),
                    "Waiting until remaining batches are completed...");
            Thread.sleep(batchWaitForBatchesToFinishDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ItemEntity getItemEntity(String line, ScopeEntity scopeEntity, String[] headers) {
        final ItemEntity itemEntity = new ItemEntity();
        itemEntity.setScope(scopeEntity);
        itemEntity.setStatus(ItemStatusModel.IMPORTED);
        itemEntity.setProperties(getProperties(line, headers));
        return itemEntity;
    }

    private Map<String, String> getProperties(String line, String[] headers) {
        final Map<String, String> properties = new HashMap<>();
        final String[] fields = line.split(",");
        for (int i = 0; i < fields.length; i++) {
            properties.put(headers[i], fields[i]);
        }
        return properties;
    }

}
