package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.cache.InterruptingScopeCache;
import org.datamigration.jpa.entity.ItemEntity;
import org.datamigration.jpa.entity.ProjectEntity;
import org.datamigration.mapper.ItemMapper;
import org.datamigration.mapper.ProjectMapper;
import org.datamigration.mapper.ScopeMapper;
import org.datamigration.model.ItemModel;
import org.datamigration.model.ProjectModel;
import org.datamigration.model.ScopeModel;
import org.datamigration.service.ItemsService;
import org.datamigration.service.ProjectsService;
import org.datamigration.service.ScopesService;
import org.datamigration.usecase.model.CreateProjectsRequestModel;
import org.datamigration.usecase.model.UpdateProjectsRequestModel;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProjectsUsecase {

    private final ProjectMapper projectMapper = Mappers.getMapper(ProjectMapper.class);
    private final ScopeMapper scopeMapper = Mappers.getMapper(ScopeMapper.class);
    private final ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);
    private final ProjectsService projectsService;
    private final ScopesService scopesService;
    private final ItemsService itemsService;
    private final InterruptingScopeCache interruptingScopeCache;

    public ProjectModel createNewProject(CreateProjectsRequestModel createProjectsRequest, String owner) {
        final ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName(createProjectsRequest.getProjectName());
        projectEntity.setOwner(owner);
        projectEntity.setCreatedDate(new Date());
        projectEntity.setLastUpdatedDate(projectEntity.getCreatedDate());
        return Optional.of(projectEntity)
                .map(projectsService::createProject)
                .map(projectMapper::projectEntityToProject)
                .orElse(null);
    }

    public ProjectModel getProject(UUID projectId, String owner) {
        final ProjectEntity projectEntity = projectsService.getProject(projectId, owner);
        return projectMapper.projectEntityToProject(projectEntity);
    }

    public Page<ProjectModel> getAllProjects(String owner, Pageable pageable) {
        final Page<ProjectEntity> projectEntityPage = projectsService.getAll(owner, pageable);
        final List<ProjectModel> project = projectEntityPage.stream()
                .map(projectMapper::projectEntityToProject)
                .toList();
        return new PageImpl<>(project, projectEntityPage.getPageable(), projectEntityPage.getTotalElements());
    }

    public ProjectModel updateProject(UpdateProjectsRequestModel updateProjectsRequest, String owner) {
        final ProjectEntity projectEntity = projectsService.getProject(updateProjectsRequest.getProjectId(), owner);
        projectEntity.setName(updateProjectsRequest.getProjectName());
        projectEntity.setLastUpdatedDate(new Date());
        return Optional.of(projectEntity)
                .map(projectsService::updateProject)
                .map(projectMapper::projectEntityToProject)
                .orElse(null);
    }

    public ScopeModel createOrGetScope(UUID projectId, String scopeKey, boolean external, String owner) {
        return Optional.of(projectsService.getProject(projectId, owner))
                .map(projectEntity ->  scopesService.createOrGetScope(projectEntity, scopeKey, external))
                .map(scopeMapper::scopeEntityToScope)
                .orElse(null);
    }

    public void interruptScope(UUID projectId, UUID scopeId, String owner) {
        projectsService.isPermitted(projectId, owner);
        interruptingScopeCache.getInterruptingScopes().add(scopeId);
    }

    public List<ScopeModel> getAllScopes(UUID projectId, String owner) {
        projectsService.isPermitted(projectId, owner);
        return scopesService.getAll(projectId).stream()
                .map(scopeMapper::scopeEntityToScope)
                .toList();
    }

    public Page<ItemModel> getAllItems(UUID projectId, UUID scopeId, String owner, Pageable pageable) {
        projectsService.isPermitted(projectId, owner);
        final Page<ItemEntity> itemEntityPage = itemsService.getAll(scopeId, pageable);
        final List<ItemModel> item = itemEntityPage.stream()
                .map(itemMapper::itemEntityToItem)
                .toList();
        return new PageImpl<>(item, itemEntityPage.getPageable(), itemEntityPage.getTotalElements());
    }

    public void deleteScope(UUID projectId, UUID scopeId, String owner) {
        projectsService.isPermitted(projectId, owner);
        scopesService.delete(scopeId);
    }
}
