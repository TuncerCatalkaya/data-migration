package org.datamigration.usecase;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.datamigration.cache.DataMigrationCache;
import org.datamigration.service.CheckpointsService;
import org.datamigration.service.ItemsService;
import org.datamigration.service.MappingsService;
import org.datamigration.service.ProjectsService;
import org.datamigration.service.ScopesService;
import org.datamigration.usecase.api.CheckpointsMethods;
import org.datamigration.usecase.api.ItemsMethods;
import org.datamigration.usecase.api.MappingsMethods;
import org.datamigration.usecase.api.ProjectsMethods;
import org.datamigration.usecase.api.ScopesMethods;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectsUsecase {

    private final ProjectsService projectsService;
    private final ScopesService scopesService;
    private final ItemsService itemsService;
    private final CheckpointsService checkpointsService;
    private final MappingsService mappingsService;
    private final DataMigrationCache dataMigrationCache;

    @Getter(lazy = true)
    private final ProjectsMethods projectsMethods = new Projects(projectsService);

    @Getter(lazy = true)
    private final ScopesMethods scopesMethods = new Scopes(projectsService, scopesService, dataMigrationCache);

    @Getter(lazy = true)
    private final ItemsMethods itemsMethods = new Items(projectsService, scopesService, itemsService);

    @Getter(lazy = true)
    private final CheckpointsMethods checkpointsMethods = new Checkpoints(projectsService, scopesService, checkpointsService);

    @Getter(lazy = true)
    private final MappingsMethods mappingsMethods = new Mappings(projectsService, scopesService, mappingsService);
}
