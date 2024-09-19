package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.service.CheckpointsService;
import org.datamigration.usecase.model.CurrentCheckpointStatusModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CheckpointsUsecase {

    private final ProjectsUsecase projectsUsecase;
    private final ScopesUsecase scopesUsecase;
    private final CheckpointsService checkpointsService;

    public int createOrGetCheckpointBy(ScopeEntity scopeEntity, long lineCount, int batchSize) {
        return checkpointsService.createOrGetCheckpointBy(scopeEntity, lineCount, batchSize);
    }

    public CurrentCheckpointStatusModel getCurrentCheckpointStatus(UUID projectId, UUID scopeId, String owner) {
        projectsUsecase.isPermitted(projectId, owner);
        final ScopeEntity scopeEntity = scopesUsecase.get(scopeId);
        return checkpointsService.getCurrentCheckpointStatus(scopeEntity);
    }

    public boolean isBatchAlreadyProcessed(UUID scopeId, long batchIndex) {
        return checkpointsService.isBatchAlreadyProcessed(scopeId, batchIndex);
    }

    public void deleteByScopeId(UUID scopeId) {
        checkpointsService.deleteByScopeId(scopeId);
    }

}
