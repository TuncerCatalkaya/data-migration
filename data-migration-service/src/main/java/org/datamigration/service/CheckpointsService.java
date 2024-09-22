package org.datamigration.service;

import lombok.RequiredArgsConstructor;
import org.datamigration.cache.ProcessingScopeCache;
import org.datamigration.exception.CheckpointNotFoundException;
import org.datamigration.jpa.entity.CheckpointEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.jpa.repository.JpaCheckpointBatchesRepository;
import org.datamigration.jpa.repository.JpaCheckpointRepository;
import org.datamigration.usecase.model.CurrentCheckpointStatusResponseModel;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckpointsService {

    private final JpaCheckpointRepository jpaCheckpointRepository;
    private final JpaCheckpointBatchesRepository jpaCheckpointBatchesRepository;
    private final ProcessingScopeCache processingScopeCache;

    public int createOrGetCheckpointBy(ScopeEntity scopeEntity, long lineCount, int batchSize) {
        return jpaCheckpointRepository.findByScope_Id(scopeEntity.getId())
                .map(CheckpointEntity::getBatchSize)
                .orElseGet(() -> {
                    final CheckpointEntity checkpointEntity = new CheckpointEntity();
                    checkpointEntity.setScope(scopeEntity);
                    checkpointEntity.setBatchSize(batchSize);
                    checkpointEntity.setTotalBatches((long) Math.ceil((double) lineCount / batchSize));
                    jpaCheckpointRepository.save(checkpointEntity);
                    return batchSize;
                });
    }

    public CurrentCheckpointStatusResponseModel getCurrentCheckpointStatus(ScopeEntity scopeEntity) {
        long batchesProcessed = -1;
        long totalBatches = -1;
        if (scopeEntity.getCheckpoint() != null) {
            batchesProcessed = jpaCheckpointBatchesRepository.countBatchIndexByScopeId(scopeEntity.getId());
            totalBatches = scopeEntity.getCheckpoint().getTotalBatches();
        }
        return CurrentCheckpointStatusResponseModel.builder()
                .batchesProcessed(batchesProcessed)
                .totalBatches(totalBatches)
                .processing(processingScopeCache.getProcessingScopes().contains(scopeEntity.getId()))
                .finished(scopeEntity.isFinished())
                .external(scopeEntity.isExternal())
                .build();
    }

    public boolean isBatchAlreadyProcessed(UUID scopeId, long batchIndex) {
        return jpaCheckpointBatchesRepository.existsByCheckpoint_ScopeIdAndBatchIndex(scopeId, batchIndex);
    }

    public CheckpointEntity getCheckpoint(UUID scopeId) {
        return jpaCheckpointRepository.findByScope_Id(scopeId)
                .orElseThrow(() -> new CheckpointNotFoundException("Checkpoint of scope " + scopeId + " not found."));
    }

    public void deleteByScopeId(UUID scopeId) {
        jpaCheckpointRepository.deleteByScope_Id(scopeId);
    }
}
