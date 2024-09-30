package org.datamigration.service.importdata;

import lombok.RequiredArgsConstructor;
import org.datamigration.cache.DataMigrationCache;
import org.datamigration.exception.FileTypeNotSupportedException;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.model.BatchConfigModel;
import org.datamigration.service.CheckpointsService;
import org.datamigration.service.ImportDataService;
import org.datamigration.service.ScopesService;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.datamigration.logger.BatchProcessingLogger.log;

@Service
@RequiredArgsConstructor
class ImportDataServiceImpl implements ImportDataService {

    private final ScopesService scopesService;
    private final CheckpointsService checkpointsService;
    private final BatchProcessingService batchProcessingService;
    private final DataMigrationCache dataMigrationCache;
    private final BatchConfigModel batchConfig;

    public boolean importData(Callable<InputStream> inputStreamCallable, UUID projectId, UUID scopeId, long lineCount,
                              char delimiter) {
        final ScopeEntity scopeEntity = scopesService.get(scopeId);
        final String scopeKey = scopeEntity.getKey();
        if (!scopeKey.toLowerCase().endsWith("csv".toLowerCase())) {
            throw new FileTypeNotSupportedException("File type is not supported.");
        }
        long startTime = System.currentTimeMillis();

        boolean success = false;
        int attempt = 0;

        if (scopeEntity.isDelete()) {
            log(Level.WARN, scopeKey, scopeId, "Scope is marked for deletion, skipping batch processing.");
            return false;
        }
        if (!dataMigrationCache.getProcessingScopes().add(scopeId)) {
            log(Level.WARN, scopeKey, scopeId, "Scope is already being processed, skipping batch processing.");
            return false;
        }
        if (scopeEntity.isFinished()) {
            log(Level.INFO, scopeKey, scopeId, "Scope was already successfully processed, skipping batch processing.");
            dataMigrationCache.getProcessingScopes().remove(scopeId);
            return true;
        }

        try {
            while (true) {
                final int batchRetryScopeMax = batchConfig.getBatchRetryScopeMax();
                if ((!(attempt < batchRetryScopeMax && !success)) ||
                        (dataMigrationCache.getMarkedForDeletionScopes().contains(scopeId) ||
                                dataMigrationCache.getInterruptingScopes().contains(scopeId))) {
                    break;
                }
                attempt++;

                log(Level.INFO, scopeKey, scopeId, "Starting attempt " + attempt + " of " + batchRetryScopeMax + ".");

                final int batchSizeFromConfig = batchConfig.getBatchSize();
                final int batchSize = checkpointsService.createOrGetCheckpointBy(scopeEntity, lineCount, batchSizeFromConfig);

                success =
                        batchProcessingService.batchProcessing(inputStreamCallable, projectId, scopeEntity, batchSize, startTime,
                                attempt, delimiter);
            }

            if (!success) {
                log(Level.ERROR, scopeKey, scopeId,
                        "All retries failed. " +
                                "Batch processing aborted. " +
                                "This could be to an error, " +
                                "a manual interruption or because the object got deleted during processing. " +
                                "Please check logs for further information.");
                if (!scopeEntity.isExternal()) {
                    scopesService.markForDeletion(scopeId);
                }
            } else {
                checkpointsService.deleteByScopeId(scopeId);
            }

            return success;
        } catch (Exception ex) {
            log(Level.ERROR, scopeKey, scopeId, "Error occurred: " + ex.getMessage());
            return false;
        } finally {
            dataMigrationCache.getProcessingScopes().remove(scopeId);
            dataMigrationCache.getInterruptingScopes().remove(scopeId);
        }
    }

}
