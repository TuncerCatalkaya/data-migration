package org.datamigration.usecase;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.datamigration.domain.exception.ProjectForbiddenException;
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
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.BufferedReader;
import java.io.IOException;
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

    private final int MAX_RETRY_BATCH = 5;

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

    public void importFromS3(String bucket, String key, String owner) throws ProjectForbiddenException {
        long startTime = System.currentTimeMillis();

        final ResponseInputStream<GetObjectResponse> object = s3Usecase.getObject(bucket, key, owner);
        final UUID projectId = DataMigrationUtils.getProjectIdFromS3Key(key);
        final String fileName = DataMigrationUtils.getFileNameFromS3Key(key);
        final int batchSize;

        final ScopeModel scopeModel = projectsUsecase.addInputScope(projectId, fileName);
        final ScopeEntity scopeEntity = jpaScopeRepository.findById(scopeModel.getId()).orElseThrow();
        if (scopeEntity.isFinished()) {
            BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(),
                    "Scope was already successfully processed, skipping batch processing.");
            return;
        }

        final Optional<CheckpointEntity> foundCheckpointEntity = jpaCheckpointRepository.findByScope_Id(scopeEntity.getId());
        if (foundCheckpointEntity.isPresent()) {
            batchSize = foundCheckpointEntity.get().getBatchSize();
        } else {
            batchSize = BATCH_SIZE;
            final CheckpointEntity checkpointEntity = getCheckpointEntity(scopeEntity, batchSize);
            jpaCheckpointRepository.save(checkpointEntity);
        }

        try (InputStream inputStream = object; BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

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
                            handleBatch(activeBatches, batchProcessing, failed);
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
                handleBatch(activeBatches, batchProcessing, failed);
            }

            if (!failed.get()) {
                while (activeBatches.get() > 0) {
                    try {
                        BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(), "Waiting until remaining batches are completed...");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }
                }

                jpaScopeRepository.finish(scopeEntity.getId());
                long estimatedTime = System.currentTimeMillis() - startTime;
                BatchProcessingLogger.log(Level.INFO, fileName, scopeEntity.getId(),
                        "All batches processed. Total time: " + estimatedTime + " ms.");
            } else {
                BatchProcessingLogger.log(Level.ERROR, fileName, scopeEntity.getId(),
                        "Batch processing was interrupted due to a failure.");
            }

        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void handleBatch(AtomicLong activeBatches, BatchProcessingModel batchProcessing, AtomicBoolean failed) {
        activeBatches.incrementAndGet();
        processBatchAsync(batchProcessing).whenComplete((result, ex) -> {
            if (ex != null) {
                BatchProcessingLogger.log(Level.ERROR, batchProcessing.getFileName(), batchProcessing.getScopeId(),
                        "Fatal error during batch " + batchProcessing.getBatchIndex() +
                                " occurred. Batch process will be stopped.");
                failed.set(true);
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
