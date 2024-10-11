package org.datamigration.service.importdata;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.CheckpointEntity;
import org.datamigration.model.AsyncBatchConfigModel;
import org.datamigration.model.BatchProcessingModel;
import org.datamigration.service.BatchInsertService;
import org.datamigration.service.CheckpointsService;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
class AsyncBatchService {

    private final CheckpointsService checkpointsService;
    private final BatchInsertService batchInsertService;
    private final AsyncBatchConfigModel asyncBatchConfig;

    CompletableFuture<Void> processBatchAsync(BatchProcessingModel batchProcessing) {
        return CompletableFuture.runAsync(() -> {
            final CheckpointEntity checkpointEntity = checkpointsService.getCheckpoint(batchProcessing.getScopeId());
            batchInsertService.insertBatch(batchProcessing, checkpointEntity);
        }, asyncBatchConfig.getExecutorService());
    }

}
