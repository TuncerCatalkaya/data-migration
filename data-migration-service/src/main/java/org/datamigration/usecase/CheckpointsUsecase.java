package org.datamigration.usecase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    private final JpaCheckpointRepository jpaCheckpointRepository;
    private final JpaCheckpointBatchesRepository jpaCheckpointBatchesRepository;

    public int createOrGetCheckpointBy(ScopeEntity scopeEntity, long lineCount, int batchSizeGet) {
        final int batchSize;
        if (jpaCheckpointRepository.existsByScope_Id(scopeEntity.getId())) {
            batchSize = jpaCheckpointRepository.findBatchSizeByScopeId(scopeEntity.getId());
        } else {
            batchSize = batchSizeGet;
            final CheckpointEntity checkpointEntity = new CheckpointEntity();
            checkpointEntity.setScope(scopeEntity);
            checkpointEntity.setBatchSize(batchSize);
            checkpointEntity.setTotalBatches((long) Math.ceil((double) lineCount / batchSize));
            jpaCheckpointRepository.save(checkpointEntity);
        }
        return batchSize;
    }

    @Transactional
    public CurrentCheckpointStatusModel getCurrentCheckpointStatus(UUID projectId, UUID scopeId, String owner)
            throws ProjectForbiddenException {
        projectsUsecase.isPermitted(projectId, owner);
        final long batchesProcessed = jpaCheckpointBatchesRepository.countBatchIndexByScopeId(scopeId);
        final long totalBatches = jpaCheckpointRepository.findTotalBatchesByScopeId(scopeId);
        return CurrentCheckpointStatusModel.builder()
                .batchesProcessed(batchesProcessed)
                .totalBatches(totalBatches)
                .build();
    }

    public boolean isBatchAlreadyProcessed(UUID scopeId, long batchIndex) {
        return jpaCheckpointBatchesRepository.existsByCheckpoint_ScopeIdAndBatchIndex(scopeId, batchIndex);
    }

    public void deleteByScopeId(UUID scopeId) {
        jpaCheckpointRepository.deleteByScope_Id(scopeId);
    }

}
