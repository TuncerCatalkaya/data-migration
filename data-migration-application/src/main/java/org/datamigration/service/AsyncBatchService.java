package org.datamigration.service;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.CheckpointEntity;
import org.datamigration.logger.BatchProcessingLogger;
import org.datamigration.model.BatchProcessingModel;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class AsyncBatchService {

    @Value("${batch.threads}")
    private int batchThreadsEnv;
    @Value("${batch.retry.batch.delayMs}")
    private long batchRetryBatchDelayMs;
    @Value("${batch.waitForFullQueue.delayMs}")
    private long batchWaitForFullQueueDelayMs;

    private final BatchService batchService;
    private final CheckpointsService checkpointsService;

    private final Object lock = new Object();

    public void handleBatch(BatchProcessingModel batchProcessing, AtomicBoolean failed, AtomicLong activeBatches,
                            AtomicLong activeBatchesScope, int remainingRetries, ExecutorService executorService) {
        synchronized (lock) {
            while (activeBatches.get() >= batchThreadsEnv) {
                waitForFullQueue(batchProcessing.getScopeKey(), batchProcessing.getScopeId());
            }
            activeBatches.incrementAndGet();
            activeBatchesScope.incrementAndGet();
        }

        retryBatch(batchProcessing, failed, activeBatches, activeBatchesScope, remainingRetries, executorService);
    }

    private void retryBatch(BatchProcessingModel batchProcessing, AtomicBoolean failed, AtomicLong activeBatches,
                            AtomicLong activeBatchesScope, int remainingRetries, ExecutorService executorService) {
        final AtomicBoolean fatal = new AtomicBoolean(false);
        processBatchAsync(batchProcessing, executorService).whenComplete((result, ex) -> {
            activeBatches.decrementAndGet();
            activeBatchesScope.decrementAndGet();
            if (activeBatches.get() < 0) {
                activeBatches.set(0);
            }
            if (activeBatchesScope.get() < 0) {
                activeBatchesScope.set(0);
            }
            if (ex != null) {
                final String errorPrefix = "Error during batch " + batchProcessing.getBatchIndex() + ". ";
                BatchProcessingLogger.log(Level.ERROR, batchProcessing.getScopeKey(), batchProcessing.getScopeId(),
                        errorPrefix + ex.getMessage());
                if (ex.getCause() instanceof CannotCreateTransactionException) {
                    fatal.set(true);
                    BatchProcessingLogger.log(Level.ERROR, batchProcessing.getScopeKey(), batchProcessing.getScopeId(),
                            errorPrefix + "Error is fatal, retries will be skipped.");
                }
                if (!fatal.get() && remainingRetries > 1) {
                    BatchProcessingLogger.log(Level.WARN, batchProcessing.getScopeKey(), batchProcessing.getScopeId(),
                            errorPrefix + "Retrying... Remaining retries: " + (remainingRetries - 1));
                    try {
                        Thread.sleep(batchRetryBatchDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    retryBatch(batchProcessing, failed, activeBatches, activeBatchesScope, remainingRetries - 1,
                            executorService);
                } else {
                    BatchProcessingLogger.log(Level.ERROR, batchProcessing.getScopeKey(), batchProcessing.getScopeId(),
                            errorPrefix + "Batch processing will be stopped.");
                    failed.set(true);
                }
            } else {
                BatchProcessingLogger.log(Level.DEBUG, batchProcessing.getScopeKey(), batchProcessing.getScopeId(),
                        "Processed batch " + batchProcessing.getBatchIndex());
            }
        });
    }

    private CompletableFuture<Void> processBatchAsync(BatchProcessingModel batchProcessing, ExecutorService executorService) {
        return CompletableFuture.runAsync(() -> {
            final CheckpointEntity checkpointEntity = checkpointsService.getCheckpoint(batchProcessing.getScopeId());
            batchService.processBatch(batchProcessing, checkpointEntity);
        }, executorService);
    }

    private void waitForFullQueue(String scopeKey, UUID scopeId) {
        try {
            BatchProcessingLogger.log(Level.TRACE, scopeKey, scopeId, "Queue is full, waiting until a batch is completed...");
            Thread.sleep(batchWaitForFullQueueDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
