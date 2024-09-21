package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.service.CheckpointsService;
import org.datamigration.service.ProjectsService;
import org.datamigration.service.ScopesService;
import org.datamigration.usecase.model.CurrentCheckpointStatusResponseModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CheckpointsUsecase {

    private final ProjectsService projectsService;
    private final ScopesService scopesService;
    private final CheckpointsService checkpointsService;

    public CurrentCheckpointStatusResponseModel getCurrentCheckpointStatus(UUID projectId, UUID scopeId, String owner) {
        projectsService.isPermitted(projectId, owner);
        final ScopeEntity scopeEntity = scopesService.get(scopeId);
        return checkpointsService.getCurrentCheckpointStatus(scopeEntity);
    }

}
