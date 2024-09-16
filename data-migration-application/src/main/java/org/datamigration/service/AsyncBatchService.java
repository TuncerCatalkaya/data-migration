package org.datamigration.service;

import lombok.RequiredArgsConstructor;
import org.datamigration.usecase.model.BatchProcessingModel;
import org.datamigration.utils.BatchProcessingLogger;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class AsyncBatchService {

    private final BatchService batchService;

    public void handleBatch(BatchProcessingModel batchProcessing, AtomicBoolean failed, AtomicLong activeBatches, int remainingRetries, long batchRetryDelayMs, ExecutorService executorService) {
        activeBatches.incrementAndGet();
        final AtomicBoolean fatal = new AtomicBoolean(false);
        processBatchAsync(batchProcessing, executorService).whenComplete((result, ex) -> {
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
                        Thread.sleep(batchRetryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    handleBatch(batchProcessing, failed, activeBatches, remainingRetries - 1, batchRetryDelayMs, executorService);
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

    private CompletableFuture<Void> processBatchAsync(BatchProcessingModel batchProcessing, ExecutorService executorService) {
        return CompletableFuture.runAsync(() -> batchService.processBatch(batchProcessing), executorService);
    }

}
