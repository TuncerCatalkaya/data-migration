package org.datamigration.usecase;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.datamigration.cache.DataMigrationCache;
import org.datamigration.exception.FileTypeNotSupportedException;
import org.datamigration.jpa.entity.ItemEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.logger.BatchProcessingLogger;
import org.datamigration.model.BatchProcessingModel;
import org.datamigration.model.DelimiterModel;
import org.datamigration.model.ItemPropertiesModel;
import org.datamigration.service.AsyncBatchService;
import org.datamigration.service.CheckpointsService;
import org.datamigration.service.ProjectsService;
import org.datamigration.service.S3Service;
import org.datamigration.service.ScopesService;
import org.datamigration.utils.DataMigrationUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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

    private final ProjectsService projectsService;
    private final ScopesService scopesService;
    private final CheckpointsService checkpointsService;
    private final S3Service s3Service;
    private final AsyncBatchService asyncBatchService;
    private final DataMigrationCache dataMigrationCache;

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

    @Async
    public void importFromFile(byte[] bytes, UUID projectId, UUID scopeId, char delimiter, String owner) {
        projectsService.isPermitted(projectId, owner);
        final Callable<InputStream> inputStreamCallable = () -> new ByteArrayInputStream(bytes);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
            final long lineCount = reader.lines().count() - 1;
            importData(inputStreamCallable, projectId, scopeId, lineCount, delimiter);
        } catch (IOException ex) {
            throw new IllegalStateException();
        }
    }

    @Async
    public void importFromS3(UUID scopeId, String bucket, String key, String owner) {
        final UUID projectId = DataMigrationUtils.getProjectIdFromS3Key(key);
        projectsService.isPermitted(projectId, owner);
        final Callable<InputStream> inputStreamCallable = () -> s3Service.getS3Object(bucket, key);
        final long lineCount = Long.parseLong(s3Service.getS3ObjectTag(bucket, key, "lineCount"));
        final char delimiter =
                DelimiterModel.toCharacter(DelimiterModel.valueOf(s3Service.getS3ObjectTag(bucket, key, "delimiter")));
        final boolean success = importData(inputStreamCallable, projectId, scopeId, lineCount, delimiter);
        if (success) {
            s3Service.deleteObject(bucket, key);
        }
    }

    private boolean importData(Callable<InputStream> inputStreamCallable, UUID projectId, UUID scopeId,
                               long lineCount, char delimiter) {
        final ScopeEntity scopeEntity = scopesService.get(scopeId);
        if (!scopeEntity.getKey().toLowerCase().endsWith("csv".toLowerCase())) {
            throw new FileTypeNotSupportedException("File type is not supported.");
        }
        long startTime = System.currentTimeMillis();

        boolean success = false;
        int attempt = 0;

        if (!dataMigrationCache.getProcessingScopes().add(scopeEntity.getId())) {
            BatchProcessingLogger.log(Level.WARN, scopeEntity.getKey(), scopeEntity.getId(),
                    "Scope is already being processed, skipping batch processing.");
            return false;
        }
        if (scopeEntity.isFinished()) {
            BatchProcessingLogger.log(Level.INFO, scopeEntity.getKey(), scopeEntity.getId(),
                    "Scope was already successfully processed, skipping batch processing.");
            dataMigrationCache.getProcessingScopes().remove(scopeEntity.getId());
            return true;
        }

        try {
            while (attempt < batchRetryScopeMax && !success) {
                if (dataMigrationCache.getInterruptingScopes().contains(scopeEntity.getId())) {
                    final String interruptMsg = "Process was interrupted manually.";
                    BatchProcessingLogger.log(Level.WARN, scopeEntity.getKey(), scopeEntity.getId(), interruptMsg);
                    break;
                }
                attempt++;

                BatchProcessingLogger.log(Level.INFO, scopeEntity.getKey(), scopeEntity.getId(),
                        "Starting attempt " + attempt + " of " + batchRetryScopeMax + ".");

                final int batchSize = checkpointsService.createOrGetCheckpointBy(scopeEntity, lineCount, batchSizeEnv);

                success =
                        batchProcessing(inputStreamCallable, projectId, scopeEntity, batchSize, startTime, attempt, delimiter);
            }

            if (!success) {
                BatchProcessingLogger.log(Level.ERROR, scopeEntity.getKey(), scopeEntity.getId(),
                        "All retries failed. Batch processing aborted.");
                if (!scopeEntity.isExternal()) {
                    scopesService.delete(scopeEntity.getId());
                }
            } else {
                checkpointsService.deleteByScopeId(scopeEntity.getId());
            }

            return success;
        } catch (Exception ex) {
            BatchProcessingLogger.log(Level.ERROR, scopeEntity.getKey(), scopeEntity.getId(),
                    "Error occurred: " + ex.getMessage());
            return false;
        } finally {
            dataMigrationCache.getProcessingScopes().remove(scopeEntity.getId());
            dataMigrationCache.getInterruptingScopes().remove(scopeEntity.getId());
        }
    }

    private boolean batchProcessing(Callable<InputStream> inputStreamCallable, UUID projectId, ScopeEntity scopeEntity,
                                    int batchSize, long startTime, int attempt, char delimiter) {
        try (InputStream inputStream = inputStreamCallable.call();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            final String firstLine = reader.readLine();
            int arraySize = (int) firstLine.chars()
                    .filter(c -> c == delimiter)
                    .count() + 1;
            final String[] headers = DataMigrationUtils.fastSplit(firstLine, delimiter, arraySize);
            if (scopeEntity.getHeaders() == null) {
                scopesService.updateHeaders(scopeEntity.getId(), headers);
                scopeEntity.setHeaders(headers);
            }


            final List<ItemEntity> batch = new ArrayList<>();
            final AtomicLong batchIndex = new AtomicLong(1);
            final AtomicLong lineCounter = new AtomicLong(-1);

            final AtomicLong activeBatchesScope = new AtomicLong(0);
            final AtomicBoolean failed = new AtomicBoolean(false);

            final AtomicBoolean batchAlreadyProcessedCache = new AtomicBoolean(false);

            BatchProcessingLogger.log(Level.INFO, scopeEntity.getKey(), scopeEntity.getId(), "Starting to process.");
            reader.lines()
                    .takeWhile(line -> !failed.get())
                    .forEach(line -> {
                        if (dataMigrationCache.getInterruptingScopes().contains(scopeEntity.getId())) {
                            BatchProcessingLogger.log(Level.WARN, scopeEntity.getKey(), scopeEntity.getId(),
                                    "Process was interrupted manually.");
                            failed.set(true);
                        }
                        batchIndex.set((lineCounter.incrementAndGet() / batchSize) + 1);

                        if (isBatchAlreadyProcessed(lineCounter, batchSize, scopeEntity, batchIndex,
                                batchAlreadyProcessedCache)) {
                            return;
                        }

                        final ItemEntity itemEntity = getItemEntity(line, scopeEntity, headers, lineCounter.get(), delimiter);
                        batch.add(itemEntity);

                        handleFullBatch(projectId, batch, batchSize, scopeEntity, batchIndex, failed, activeBatches,
                                activeBatchesScope);
                    });

            handleLastBatch(projectId, batch, scopeEntity, batchIndex, batchSize, failed, activeBatches, activeBatchesScope);

            while (activeBatchesScope.get() > 0) {
                waitForRemainingBatchesToFinish(scopeEntity);
            }

            if (!failed.get()) {
                if (activeBatchesScope.get() <= 0) {
                    scopesService.finish(scopeEntity.getId());
                    long estimatedTime = System.currentTimeMillis() - startTime;
                    BatchProcessingLogger.log(Level.INFO, scopeEntity.getKey(), scopeEntity.getId(),
                            "All batches processed. Total time: " + estimatedTime + " ms.");
                    return true;
                }
            } else {
                BatchProcessingLogger.log(Level.ERROR, scopeEntity.getKey(), scopeEntity.getId(),
                        "Batch processing was interrupted due to a failure.");
            }

        } catch (Exception ex) {
            BatchProcessingLogger.log(Level.ERROR, scopeEntity.getKey(), scopeEntity.getId(),
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

    private boolean isBatchAlreadyProcessed(AtomicLong lineCounter, int batchSize, ScopeEntity scopeEntity, AtomicLong batchIndex,
                                            AtomicBoolean batchAlreadyProcessedCache) {
        if (lineCounter.get() % batchSize == 0) {
            if (checkpointsService.isBatchAlreadyProcessed(scopeEntity.getId(), batchIndex.get())) {
                batchAlreadyProcessedCache.set(true);
                BatchProcessingLogger.log(Level.DEBUG, scopeEntity.getKey(), scopeEntity.getId(),
                        "Batch " + batchIndex.get() + " already processed, skipping batch.");
                return true;
            }
            batchAlreadyProcessedCache.set(false);
        }

        return batchAlreadyProcessedCache.get();
    }

    private void handleFullBatch(UUID projectId, List<ItemEntity> batch, int batchSize, ScopeEntity scopeEntity,
                                 AtomicLong batchIndex, AtomicBoolean failed, AtomicLong activeBatches,
                                 AtomicLong activeBatchesScope) {
        if (batch.size() >= batchSize) {
            final BatchProcessingModel batchProcessing = BatchProcessingModel.builder()
                    .projectId(projectId)
                    .scopeId(scopeEntity.getId())
                    .scopeKey(scopeEntity.getKey())
                    .batchIndex(batchIndex.get())
                    .batchSize(batchSize)
                    .batch(new ArrayList<>(batch))
                    .build();
            asyncBatchService.handleBatch(batchProcessing, failed, activeBatches, activeBatchesScope, batchRetryBatchMax,
                    executorService);
            batch.clear();
        }
    }

    private void handleLastBatch(UUID projectId, List<ItemEntity> batch, ScopeEntity scopeEntity,
                                 AtomicLong batchIndex, int batchSize, AtomicBoolean failed, AtomicLong activeBatches,
                                 AtomicLong activeBatchesScope) {
        if (!batch.isEmpty()) {
            final BatchProcessingModel batchProcessing = BatchProcessingModel.builder()
                    .projectId(projectId)
                    .scopeId(scopeEntity.getId())
                    .scopeKey(scopeEntity.getKey())
                    .batchIndex(batchIndex.get())
                    .batchSize(batchSize)
                    .batch(new ArrayList<>(batch))
                    .build();
            asyncBatchService.handleBatch(batchProcessing, failed, activeBatches, activeBatchesScope, batchRetryBatchMax,
                    executorService);
        }
    }

    private void waitForRemainingBatchesToFinish(ScopeEntity scopeEntity) {
        try {
            BatchProcessingLogger.log(Level.TRACE, scopeEntity.getKey(), scopeEntity.getId(),
                    "Waiting until remaining batches are completed...");
            Thread.sleep(batchWaitForBatchesToFinishDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ItemEntity getItemEntity(String line, ScopeEntity scopeEntity, String[] headers, long lineNumber, char delimiter) {
        final ItemEntity itemEntity = new ItemEntity();
        itemEntity.setScope(scopeEntity);
        itemEntity.setLineNumber(lineNumber);
        itemEntity.setProperties(getProperties(line, headers, delimiter));
        return itemEntity;
    }

    private Map<String, ItemPropertiesModel> getProperties(String line, String[] headers, char delimiter) {
        final Map<String, ItemPropertiesModel> properties = new HashMap<>(headers.length);
        final String[] fields = DataMigrationUtils.fastSplit(line, delimiter, headers.length);
        for (int i = 0; i < fields.length; i++) {
            properties.put(headers[i], ItemPropertiesModel.builder()
                    .value(fields[i])
                    .build());
        }
        return properties;
    }

}
