package org.datamigration.service.importdata;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.ItemEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.model.BatchProcessingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
class HandleBatchService {

    private final HandleAsyncBatchService handleAsyncBatchService;

    public void handleFullBatch(UUID projectId, List<ItemEntity> batch, int batchSize, ScopeEntity scopeEntity,
                                AtomicLong batchIndex, AtomicBoolean failed, AtomicLong activeBatchesScope) {
        if (batch.size() >= batchSize) {
            final BatchProcessingModel batchProcessing = BatchProcessingModel.builder()
                    .projectId(projectId)
                    .scopeId(scopeEntity.getId())
                    .scopeKey(scopeEntity.getKey())
                    .batchIndex(batchIndex.get())
                    .batchSize(batchSize)
                    .batch(new ArrayList<>(batch))
                    .build();
            handleAsyncBatchService.handleAsyncBatch(batchProcessing, failed, activeBatchesScope);
            batch.clear();
        }
    }

    public void handleLastBatch(UUID projectId, List<ItemEntity> batch, ScopeEntity scopeEntity,
                                AtomicLong batchIndex, int batchSize, AtomicBoolean failed, AtomicLong activeBatchesScope) {
        if (!batch.isEmpty()) {
            final BatchProcessingModel batchProcessing = BatchProcessingModel.builder()
                    .projectId(projectId)
                    .scopeId(scopeEntity.getId())
                    .scopeKey(scopeEntity.getKey())
                    .batchIndex(batchIndex.get())
                    .batchSize(batchSize)
                    .batch(new ArrayList<>(batch))
                    .build();
            handleAsyncBatchService.handleAsyncBatch(batchProcessing, failed, activeBatchesScope);
        }
    }
}
