package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.service.CheckpointsService;
import org.datamigration.service.ProjectsService;
import org.datamigration.service.ScopesService;
import org.datamigration.usecase.api.CheckpointsMethods;
import org.datamigration.usecase.model.CurrentCheckpointStatusResponseModel;

import java.util.UUID;

@RequiredArgsConstructor
class Checkpoints implements CheckpointsMethods {

    private final ProjectsService projectsService;
    private final ScopesService scopesService;
    private final CheckpointsService checkpointsService;

    public CurrentCheckpointStatusResponseModel getCurrentCheckpointStatus(UUID projectId, UUID scopeId, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        final ScopeEntity scopeEntity = scopesService.get(scopeId);
        return checkpointsService.getCurrentCheckpointStatus(scopeEntity);
    }
}
