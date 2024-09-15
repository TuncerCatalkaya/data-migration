package org.datamigration.usecase;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.datamigration.domain.model.ItemStatusModel;
import org.datamigration.domain.model.ScopeModel;
import org.datamigration.jpa.entity.CheckpointEntity;
import org.datamigration.jpa.entity.ItemEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.jpa.repository.JpaCheckpointBatchesRepository;
import org.datamigration.jpa.repository.JpaCheckpointRepository;
import org.datamigration.jpa.repository.JpaScopeRepository;
import org.datamigration.model.BatchProcessingModel;
import org.datamigration.service.ImportDataService;
import org.datamigration.utils.BatchProcessingLogger;
import org.datamigration.utils.DataMigrationUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.CannotCreateTransactionException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
    private final JpaScopeRepository jpaScopeRepository;
    private final JpaCheckpointRepository jpaCheckpointRepository;
    private final JpaCheckpointBatchesRepository jpaCheckpointBatchesRepository;
    private final ImportDataService importDataService;

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

    public void importFromS3(String bucket, String key, String owner) {
        long startTime = System.currentTimeMillis();

        final UUID projectId = DataMigrationUtils.getProjectIdFromS3Key(key);
        final String fileName = DataMigrationUtils.getFileNameFromS3Key(key);

        boolean success = false;
        int attempt = 0;

        final ScopeModel scopeModel = projectsUsecase.addInputScope(projectId, fileName);

        while (attempt < BATCH_SCOPE_MAX_RETRY && !success) {
            attempt++;

            final ScopeEntity scopeEntity = jpaScopeRepository.findById(scopeModel.getId()).orElseThrow();
            BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(),
                    "Starting attempt " + attempt + " of " + BATCH_SCOPE_MAX_RETRY + ".");
            if (scopeEntity.isFinished()) {
                BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(),
                        "Scope was already successfully processed, skipping batch processing.");
                return;
            }

            final Optional<CheckpointEntity> foundCheckpointEntity = jpaCheckpointRepository.findByScope_Id(scopeEntity.getId());
            final int batchSize;
            if (foundCheckpointEntity.isPresent()) {
                batchSize = foundCheckpointEntity.get().getBatchSize();
            } else {
                batchSize = BATCH_SIZE;
                final CheckpointEntity checkpointEntity = getCheckpointEntity(scopeEntity, batchSize);
                jpaCheckpointRepository.save(checkpointEntity);
            }

            try (InputStream inputStream = s3Usecase.getObject(bucket, key, owner);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                final String[] headers = reader.readLine().split(",");

                final List<ItemEntity> batch = new ArrayList<>();
                final AtomicLong batchIndex = new AtomicLong(1);
                final AtomicBoolean failed = new AtomicBoolean(false);
                final AtomicLong lineCounter = new AtomicLong(-1);
                final AtomicBoolean batchAlreadyProcessedCache = new AtomicBoolean(false);

                final AtomicLong activeBatches = new AtomicLong(0);

                BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(), "Starting to process.");
                reader.lines()
                        .takeWhile(line -> !failed.get())
                        .forEach(line -> {
                            batchIndex.set((lineCounter.incrementAndGet() / batchSize) + 1);

                            if (lineCounter.get() % batchSize == 0) {
                                if (jpaCheckpointBatchesRepository.existsByCheckpoint_ScopeIdAndBatchIndex(scopeEntity.getId(),
                                        batchIndex.get())) {
                                    batchAlreadyProcessedCache.set(true);
                                    BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(),
                                            "Batch " + batchIndex.get() + " already processed, skipping batch.");
                                    return;
                                }
                                batchAlreadyProcessedCache.set(false);
                            }

                            if (batchAlreadyProcessedCache.get()) {
                                return;
                            }

                            final ItemEntity itemEntity = getItemEntity(line, scopeEntity, headers);
                            batch.add(itemEntity);

                            if (batch.size() >= batchSize) {
                                final BatchProcessingModel batchProcessing = BatchProcessingModel.builder()
                                        .projectId(projectId)
                                        .scopeId(scopeEntity.getId())
                                        .fileName(fileName)
                                        .batchIndex(batchIndex.get())
                                        .batchSize(batchSize)
                                        .batch(new ArrayList<>(batch))
                                        .build();
                                handleBatch(batchProcessing, BATCH_MAX_RETRY, failed, activeBatches);
                                batch.clear();
                            }
                        });

                if (!batch.isEmpty()) {
                    final BatchProcessingModel batchProcessing = BatchProcessingModel.builder()
                            .projectId(projectId)
                            .scopeId(scopeEntity.getId())
                            .fileName(fileName)
                            .batchIndex(batchIndex.get())
                            .batchSize(batchSize)
                            .batch(new ArrayList<>(batch))
                            .build();
                    handleBatch(batchProcessing, BATCH_MAX_RETRY, failed, activeBatches);
                }

                while (activeBatches.get() > 0) {
                    try {
                        BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(),
                                "Waiting until remaining batches are completed...");
                        Thread.sleep(BATCH_WAIT_ACTIVE_BATCHES_RETRY_DELAY_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
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

    private void handleBatch(BatchProcessingModel batchProcessing, int remainingRetries, AtomicBoolean failed,
                             AtomicLong activeBatches) {
        activeBatches.incrementAndGet();
        final AtomicBoolean fatal = new AtomicBoolean(false);
        processBatchAsync(batchProcessing).whenComplete((result, ex) -> {
            if (ex != null) {
                final String errorPrefix = "Error during batch " + batchProcessing.getBatchIndex() + ". ";
                if (ex.getCause() instanceof CannotCreateTransactionException) {
                    fatal.set(true);
                    BatchProcessingLogger.log(Level.ERROR, batchProcessing.getFileName(), batchProcessing.getScopeId(),
                            errorPrefix + "Error is fatal, retries will be skipped.");
                }
                if (!fatal.get() && remainingRetries > 1) {
                    BatchProcessingLogger.log(Level.WARN, batchProcessing.getFileName(), batchProcessing.getScopeId(),
                            errorPrefix + "Retrying... Remaining retries: " + (remainingRetries - 1));
                    try {
                        Thread.sleep(BATCH_RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    handleBatch(batchProcessing, remainingRetries - 1, failed, activeBatches);
                } else {
                    BatchProcessingLogger.log(Level.ERROR, batchProcessing.getFileName(), batchProcessing.getScopeId(),
                            errorPrefix + "Batch processing will be stopped.");
                    failed.set(true);
                }
            } else {
                BatchProcessingLogger.log(Level.INFO, batchProcessing.getFileName(), batchProcessing.getScopeId(),
                        "Processed batch " + batchProcessing.getBatchIndex());
            }
            activeBatches.decrementAndGet();
        });

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

    private CompletableFuture<Void> processBatchAsync(BatchProcessingModel batchProcessing) {
        return CompletableFuture.runAsync(() -> importDataService.processBatch(batchProcessing), executorService);
    }

    private static CheckpointEntity getCheckpointEntity(ScopeEntity scope, int batchSize) {
        final CheckpointEntity checkpointEntity = new CheckpointEntity();
        checkpointEntity.setScope(scope);
        checkpointEntity.setBatchSize(batchSize);
        return checkpointEntity;
    }

}
