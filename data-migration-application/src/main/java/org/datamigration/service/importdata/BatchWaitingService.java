package org.datamigration.service.importdata;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.model.BatchConfigModel;
import org.datamigration.model.BatchProcessingModel;
import org.datamigration.model.BatchWaitingConfigModel;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.datamigration.logger.BatchProcessingLogger.log;

@Service
@RequiredArgsConstructor
class BatchWaitingService {

    private final BatchConfigModel batchConfig;
    private final BatchWaitingConfigModel batchWaitingConfig;

    public void scopeRetryDelay(int attempt) {
        if (attempt < batchConfig.getBatchRetryScopeMax()) {
            try {
                Thread.sleep(batchWaitingConfig.getBatchRetryScopeDelayMs());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void batchRetryDelay() {
        try {
            Thread.sleep(batchWaitingConfig.getBatchRetryBatchDelayMs());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    public void waitForFullQueue(BatchProcessingModel batchProcessing) {
        try {
            final String scopeKey = batchProcessing.getScopeKey();
            final UUID scopeId = batchProcessing.getScopeId();
            log(Level.TRACE, scopeKey, scopeId, "Queue is full, waiting until a batch is completed...");
            Thread.sleep(batchWaitingConfig.getBatchWaitForFullQueueDelayMs());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void waitForRemainingBatchesToFinish(ScopeEntity scopeEntity) {
        try {
            final String scopeKey = scopeEntity.getKey();
            final UUID scopeId = scopeEntity.getId();
            log(Level.TRACE, scopeKey, scopeId, "Waiting until remaining batches are completed...");
            Thread.sleep(batchWaitingConfig.getBatchWaitForBatchesToFinishDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
