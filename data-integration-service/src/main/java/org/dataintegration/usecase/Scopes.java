package org.dataintegration.usecase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dataintegration.cache.DataIntegrationCache;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.mapper.ScopeMapper;
import org.dataintegration.model.ScopeModel;
import org.dataintegration.service.MappingsService;
import org.dataintegration.service.ProjectsService;
import org.dataintegration.service.ScopesService;
import org.dataintegration.usecase.api.ScopesMethods;
import org.dataintegration.usecase.model.GetScopeHeadersResponseModel;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
class Scopes implements ScopesMethods {

    private final ScopeMapper scopeMapper = Mappers.getMapper(ScopeMapper.class);
    private final ProjectsService projectsService;
    private final ScopesService scopesService;
    private final MappingsService mappingsService;
    private final DataIntegrationCache dataIntegrationCache;

    public ScopeModel createOrGetScope(UUID projectId, String scopeKey, boolean external, String createdBy) {
        return Optional.of(projectsService.getProject(projectId, createdBy))
                .map(projectEntity -> scopesService.createOrGetScope(projectEntity, scopeKey, external))
                .map(scopeMapper::scopeEntityToScope)
                .orElse(null);
    }

    public void interruptScope(UUID projectId, UUID scopeId, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        dataIntegrationCache.getInterruptingScopes().add(scopeId);
    }

    public GetScopeHeadersResponseModel getScopeHeaders(UUID projectId, UUID scopeId, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        final ScopeEntity scopeEntity = scopesService.getAndCheckIfScopeFinished(scopeId);
        return GetScopeHeadersResponseModel.builder()
                .headers(scopeEntity.getHeaders())
                .extraHeaders(scopeEntity.getExtraHeaders())
                .build();
    }

    public List<ScopeModel> getAllScopes(UUID projectId, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        return scopesService.getAll(projectId).stream()
                .map(scopeMapper::scopeEntityToScope)
                .toList();
    }

    @Transactional
    public void markScopeForDeletion(UUID projectId, UUID scopeId, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        scopesService.markForDeletion(scopeId);
        mappingsService.markForDeletionByScope(scopeId);
    }

    public void addExtraHeader(UUID projectId, UUID scopeId, String extraHeader, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        scopesService.addExtraHeader(scopeId, extraHeader);
    }

    public void removeExtraHeader(UUID projectId, UUID scopeId, String extraHeader, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        scopesService.removeExtraHeader(scopeId, extraHeader);
    }

}
