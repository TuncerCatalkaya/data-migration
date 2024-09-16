package org.datamigration.usecase;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.datamigration.domain.exception.ProjectForbiddenException;
import org.datamigration.domain.model.ItemStatusModel;
import org.datamigration.domain.model.ScopeModel;
import org.datamigration.jpa.entity.ItemEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.jpa.repository.JpaCheckpointBatchesRepository;
import org.datamigration.jpa.repository.JpaScopeRepository;
import org.datamigration.service.AsyncBatchService;
import org.datamigration.usecase.model.BatchProcessingModel;
import org.datamigration.utils.BatchProcessingLogger;
import org.datamigration.utils.DataMigrationUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class ImportDataUsecase {

    @Value("${batch.size}")
    private int BATCH_SIZE;
    @Value("${batch.thread.corePoolSize}")
    private int BATCH_THREAD_CORE_POOL_SIZE;
    @Value("${batch.thread.maximumPoolSize}")
    private int BATCH_THREAD_MAXIMUM_POOL_SIZE;

    private static final int BATCH_MAX_RETRY = 5;
    private static final long BATCH_RETRY_DELAY_MS = 2000;

    private static final int BATCH_SCOPE_MAX_RETRY = 5;
    private static final long BATCH_SCOPE_RETRY_DELAY_MS = 2000;

    private static final long BATCH_WAIT_ACTIVE_BATCHES_RETRY_DELAY_MS = 1000;

    private final S3Usecase s3Usecase;
    private final ProjectsUsecase projectsUsecase;
    private final ScopesUsecase scopesUsecase;
    private final CheckpointsUsecase checkpointsUsecase;
    private final JpaScopeRepository jpaScopeRepository;
    private final JpaCheckpointBatchesRepository jpaCheckpointBatchesRepository;
    private final AsyncBatchService asyncBatchService;

    private ExecutorService executorService;

    @PostConstruct
    void configure() {
        executorService = new ThreadPoolExecutor(
                BATCH_THREAD_CORE_POOL_SIZE,
                BATCH_THREAD_MAXIMUM_POOL_SIZE,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(BATCH_THREAD_MAXIMUM_POOL_SIZE),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    public void importFromS3(String bucket, String key, String owner) throws ProjectForbiddenException {
        s3Usecase.isPermitted(key, owner);
        final UUID projectId = DataMigrationUtils.getProjectIdFromS3Key(key);
        final String fileName = DataMigrationUtils.getFileNameFromS3Key(key);
        final Callable<InputStream> inputStreamCallable = () -> s3Usecase.getObject(bucket, key, owner);
        importData(inputStreamCallable, projectId, fileName);
    }

    public void importFromFile(MultipartFile file, UUID projectId, String fileName, String owner)
            throws ProjectForbiddenException {
        projectsUsecase.isPermitted(projectId, owner);
        final String finalFileName = Objects.requireNonNullElseGet(fileName,
                () -> FilenameUtils.getBaseName(file.getOriginalFilename()) + "-" + DataMigrationUtils.getTimeStamp() + "." +
                        FilenameUtils.getExtension(file.getName()));
        final Callable<InputStream> inputStreamCallable = file::getInputStream;
        importData(inputStreamCallable, projectId, finalFileName);
    }

    private void importData(Callable<InputStream> inputStreamCallable, UUID projectId, String fileName) {
        long startTime = System.currentTimeMillis();

        boolean success = false;
        int attempt = 0;

        final ScopeModel scopeModel = projectsUsecase.addInputScope(projectId, fileName);

        while (attempt < BATCH_SCOPE_MAX_RETRY && !success) {
            attempt++;

            final ScopeEntity scopeEntity = scopesUsecase.get(scopeModel.getId());
            BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(),
                    "Starting attempt " + attempt + " of " + BATCH_SCOPE_MAX_RETRY + ".");
            if (scopeEntity.isFinished()) {
                BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(),
                        "Scope was already successfully processed, skipping batch processing.");
                return;
            }

            final int batchSize = checkpointsUsecase.createOrGetCheckpointBy(scopeEntity, BATCH_SIZE);

            try (InputStream inputStream = inputStreamCallable.call();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                final String[] headers = reader.readLine().split(",");

                final List<ItemEntity> batch = new ArrayList<>();
                final AtomicLong batchIndex = new AtomicLong(1);
                final AtomicLong lineCounter = new AtomicLong(-1);

                final AtomicLong activeBatches = new AtomicLong(0);
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

                            handleFullBatch(projectId, fileName, batch, batchSize, scopeEntity, batchIndex, failed, activeBatches);
                        });

                handleLastBatch(projectId, fileName, batch, scopeEntity, batchIndex, batchSize, failed, activeBatches);

                while (activeBatches.get() > 0) {
                    waitForRemainingBatchesToFinish(fileName, scopeEntity);
                }

                if (!failed.get()) {
                    if (activeBatches.get() == 0) {
                        jpaScopeRepository.finish(scopeEntity.getId());
                        long estimatedTime = System.currentTimeMillis() - startTime;
                        BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(),
                                "All batches processed. Total time: " + estimatedTime + " ms.");
                        success = true;
                    }
                } else {
                    BatchProcessingLogger.log(Level.ERROR, fileName, scopeEntity.getId(),
                            "Batch processing was interrupted due to a failure.");
                }

            } catch (Exception ex) {
                BatchProcessingLogger.log(Level.ERROR, fileName, scopeEntity.getId(),
                        "Attempt " + attempt + " failed: " + ex.getMessage());
                if (attempt < BATCH_SCOPE_MAX_RETRY) {
                    try {
                        Thread.sleep(BATCH_SCOPE_RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        if (!success) {
            BatchProcessingLogger.log(Level.ERROR, fileName, scopeModel.getId(), "All retries failed. Batch processing aborted.");
        }
    }

    private boolean isBatchAlreadyProcessed(String fileName, AtomicLong lineCounter, int batchSize, ScopeEntity scopeEntity,
                              AtomicLong batchIndex, AtomicBoolean batchAlreadyProcessedCache) {
        if (lineCounter.get() % batchSize == 0) {
            if (jpaCheckpointBatchesRepository.existsByCheckpoint_ScopeIdAndBatchIndex(scopeEntity.getId(),
                    batchIndex.get())) {
                batchAlreadyProcessedCache.set(true);
                BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(),
                        "Batch " + batchIndex.get() + " already processed, skipping batch.");
                return true;
            }
            batchAlreadyProcessedCache.set(false);
        }

        return batchAlreadyProcessedCache.get();
    }

    private void handleLastBatch(UUID projectId, String fileName, List<ItemEntity> batch, ScopeEntity scopeEntity,
                           AtomicLong batchIndex, int batchSize, AtomicBoolean failed, AtomicLong activeBatches) {
        if (!batch.isEmpty()) {
            final BatchProcessingModel batchProcessing = BatchProcessingModel.builder()
                    .projectId(projectId)
                    .scopeId(scopeEntity.getId())
                    .fileName(fileName)
                    .batchIndex(batchIndex.get())
                    .batchSize(batchSize)
                    .batch(new ArrayList<>(batch))
                    .build();
            asyncBatchService.handleBatch(batchProcessing, failed, activeBatches, BATCH_MAX_RETRY, BATCH_RETRY_DELAY_MS,
                    executorService);
        }
    }

    private void handleFullBatch(UUID projectId, String fileName, List<ItemEntity> batch, int batchSize, ScopeEntity scopeEntity,
                           AtomicLong batchIndex, AtomicBoolean failed, AtomicLong activeBatches) {
        if (batch.size() >= batchSize) {
            final BatchProcessingModel batchProcessing = BatchProcessingModel.builder()
                    .projectId(projectId)
                    .scopeId(scopeEntity.getId())
                    .fileName(fileName)
                    .batchIndex(batchIndex.get())
                    .batchSize(batchSize)
                    .batch(new ArrayList<>(batch))
                    .build();
            asyncBatchService.handleBatch(batchProcessing, failed, activeBatches, BATCH_MAX_RETRY,
                    BATCH_RETRY_DELAY_MS, executorService);
            batch.clear();
        }
    }

    private static void waitForRemainingBatchesToFinish(String fileName, ScopeEntity scopeEntity) {
        try {
            BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(),
                    "Waiting until remaining batches are completed...");
            Thread.sleep(BATCH_WAIT_ACTIVE_BATCHES_RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static ItemEntity getItemEntity(String line, ScopeEntity scopeEntity, String[] headers) {
        final ItemEntity itemEntity = new ItemEntity();
        itemEntity.setScope(scopeEntity);
        itemEntity.setStatus(ItemStatusModel.IMPORTED);
        itemEntity.setProperties(getProperties(line, headers));
        return itemEntity;
    }

    private static Map<String, String> getProperties(String line, String[] headers) {
        final Map<String, String> properties = new HashMap<>();
        final String[] fields = line.split(",");
        for (int i = 0; i < fields.length; i++) {
            properties.put(headers[i], fields[i]);
        }
        return properties;
    }

}
