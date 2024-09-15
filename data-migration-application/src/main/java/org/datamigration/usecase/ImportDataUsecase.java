package org.datamigration.usecase;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.datamigration.model.ImportDataService;
import org.datamigration.utils.DataMigrationUtils;
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
@Slf4j
public class ImportDataUsecase {

    @Value("${batch.size}")
    private int BATCH_SIZE;
    @Value("${batch.thread.corePoolSize}")
    private int BATCH_THREAD_CORE_POOL_SIZE;
    @Value("${batch.thread.maximumPoolSize}")
    private int BATCH_THREAD_MAXIMUM_POOL_SIZE;

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
            log.info(fileName + "(" + scopeEntity.getId() +
                    "), Scope was already successfully processed, skipping batch processing.");
            return;
        }

        final Optional<CheckpointEntity> foundCheckpointEntity = jpaCheckpointRepository.findById(scopeEntity.getId());
        if (foundCheckpointEntity.isPresent()) {
            batchSize = foundCheckpointEntity.get().getBatchSize();
        } else {
            batchSize = BATCH_SIZE;
            final CheckpointEntity checkpointEntity = getCheckpointEntity(projectId, scopeEntity.getId(), batchSize);
            jpaCheckpointRepository.save(checkpointEntity);
        }

        try (InputStream inputStream = object; BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            final String[] headers = reader.readLine().split(",");

            final List<ItemEntity> batch = new ArrayList<>();
            final List<CompletableFuture<Void>> futures = new ArrayList<>();
            final AtomicLong batchIndex = new AtomicLong(1);
            final AtomicBoolean failed = new AtomicBoolean(false);
            final AtomicLong lineCounter = new AtomicLong(-1);
            final AtomicBoolean batchAlreadyProcessedCache = new AtomicBoolean(false);

            log.info("Starting to process: " + fileName + "(" + scopeEntity.getId() + ")");
            reader.lines()
                    .takeWhile(line -> !failed.get())
                    .forEach(line -> {
                        batchIndex.set((lineCounter.incrementAndGet() / batchSize) + 1);

                        if (lineCounter.get() % batchSize == 0) {
                            if (jpaCheckpointBatchesRepository.existsByCheckpoint_ScopeIdAndBatchIndex(scopeEntity.getId(),
                                    batchIndex.get())) {
                                batchAlreadyProcessedCache.set(true);
                                log.info(fileName + "(" + scopeEntity.getId() + "), Batch " + batchIndex.get() +
                                        " already processed.");
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
                            handleFuture(futures, failed, batchProcessing);
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
                handleFuture(futures, failed, batchProcessing);
            }

            final CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.thenRun(() -> {
                if (!failed.get()) {
                    jpaScopeRepository.finish(scopeEntity.getId());
                    long estimatedTime = System.currentTimeMillis() - startTime;
                    log.info(fileName + "(" + scopeEntity.getId() + "), All batches processed. Total time: " + estimatedTime +
                            " ms.");
                } else {
                    log.error(fileName + "(" + scopeEntity.getId() + "), Batch processing was interrupted due to a failure.");
                }
            }).join();

        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void handleFuture(List<CompletableFuture<Void>> futures, AtomicBoolean failed, BatchProcessingModel batchProcessing) {
        final CompletableFuture<Void> future = processBatchAsync(batchProcessing);
        futures.add(future);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error(
                        "Something went terribly wrong, if you see the message you should immediately panic and make sure to uninstall everything!!!");
                futures.forEach(f -> f.cancel(true));
                failed.set(true);
            }
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
        return CompletableFuture.runAsync(() -> importDataService.processBatchAsync(batchProcessing), executorService);
    }

    private static CheckpointEntity getCheckpointEntity(UUID projectId, UUID scopeId, int batchSize) {
        final CheckpointEntity checkpointEntity = new CheckpointEntity();
        checkpointEntity.setProjectId(projectId);
        checkpointEntity.setScopeId(scopeId);
        checkpointEntity.setBatchSize(batchSize);
        return checkpointEntity;
    }

}
