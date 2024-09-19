package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.cache.ProcessingScopeCache;
import org.datamigration.domain.exception.ProjectForbiddenException;
import org.datamigration.jpa.entity.CheckpointEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.jpa.repository.JpaCheckpointBatchesRepository;
import org.datamigration.jpa.repository.JpaCheckpointRepository;
import org.datamigration.usecase.model.CurrentCheckpointStatusModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CheckpointsUsecase {

    private final ProjectsUsecase projectsUsecase;
    private final ScopesUsecase scopesUsecase;
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

    public CurrentCheckpointStatusModel getCurrentCheckpointStatus(UUID projectId, UUID scopeId, String owner)
            throws ProjectForbiddenException {
        projectsUsecase.isPermitted(projectId, owner);
        final ScopeEntity scopeEntity = scopesUsecase.get(scopeId);
        if (scopeEntity.getCheckpoint() != null) {
            final long batchesProcessed = jpaCheckpointBatchesRepository.countBatchIndexByScopeId(scopeId);
            return CurrentCheckpointStatusModel.builder()
                    .batchesProcessed(batchesProcessed)
                    .totalBatches(scopeEntity.getCheckpoint().getTotalBatches())
                    .processing(processingScopeCache.getProcessingScopes().contains(scopeId))
                    .finished(scopeEntity.isFinished())
                    .build();
        } else {
            return CurrentCheckpointStatusModel.builder()
                    .batchesProcessed(-1)
                    .totalBatches(-1)
                    .processing(processingScopeCache.getProcessingScopes().contains(scopeId))
                    .finished(scopeEntity.isFinished())
                    .build();
        }
    }

    public boolean isBatchAlreadyProcessed(UUID scopeId, long batchIndex) {
        return jpaCheckpointBatchesRepository.existsByCheckpoint_ScopeIdAndBatchIndex(scopeId, batchIndex);
    }

    public void deleteByScopeId(UUID scopeId) {
        jpaCheckpointRepository.deleteByScope_Id(scopeId);
    }

}
