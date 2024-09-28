package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.cache.DataMigrationCache;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.mapper.ScopeMapper;
import org.datamigration.model.ScopeModel;
import org.datamigration.service.ProjectsService;
import org.datamigration.service.ScopesService;
import org.datamigration.usecase.api.ScopesMethods;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
class Scopes implements ScopesMethods {

    private final ScopeMapper scopeMapper = Mappers.getMapper(ScopeMapper.class);
    private final ProjectsService projectsService;
    private final ScopesService scopesService;
    private final DataMigrationCache dataMigrationCache;

    public ScopeModel createOrGetScope(UUID projectId, String scopeKey, boolean external, String owner) {
        return Optional.of(projectsService.getProject(projectId, owner))
                .map(projectEntity -> scopesService.createOrGetScope(projectEntity, scopeKey, external))
                .map(scopeMapper::scopeEntityToScope)
                .orElse(null);
    }

    public void interruptScope(UUID projectId, UUID scopeId, String owner) {
        projectsService.isPermitted(projectId, owner);
        dataMigrationCache.getInterruptingScopes().add(scopeId);
    }

    public String[] getScopeHeaders(UUID projectId, UUID scopeId, String owner) {
        projectsService.isPermitted(projectId, owner);
        final ScopeEntity scopeEntity = scopesService.get(scopeId);
        return scopeEntity.getHeaders();
    }

    public List<ScopeModel> getAllScopes(UUID projectId, String owner) {
        projectsService.isPermitted(projectId, owner);
        return scopesService.getAll(projectId).stream()
                .map(scopeMapper::scopeEntityToScope)
                .toList();
    }

    public void deleteScope(UUID projectId, UUID scopeId, String owner) {
        projectsService.isPermitted(projectId, owner);
        scopesService.delete(scopeId);
    }

}
