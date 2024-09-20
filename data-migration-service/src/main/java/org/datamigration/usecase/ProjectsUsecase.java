package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.ProjectEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.mapper.ProjectInformationMapper;
import org.datamigration.model.ProjectInformationModel;
import org.datamigration.service.ProjectsService;
import org.datamigration.usecase.model.CreateProjectsRequestModel;
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

    private final ProjectInformationMapper projectInformationMapper = Mappers.getMapper(ProjectInformationMapper.class);
    private final ProjectsService projectsService;

    public void isPermitted(UUID projectId, String owner) {
        projectsService.isPermitted(projectId, owner);
    }

    public ProjectInformationModel createNew(CreateProjectsRequestModel createProjectsRequest, String owner) {
        final ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName(createProjectsRequest.getProjectName());
        projectEntity.setOwner(owner);
        projectEntity.setCreatedDate(new Date());
        projectEntity.setLastUpdatedDate(projectEntity.getCreatedDate());
        return Optional.of(projectEntity)
                .map(projectsService::createOrUpdateProject)
                .map(projectInformationMapper::projectEntityToProjectInformation)
                .orElse(null);
    }

    public ScopeEntity createOrGetScope(UUID projectId, String scopeKey, boolean external) {
        return projectsService.createOrGetScope(projectId, scopeKey, external);
    }

    public ProjectInformationModel get(UUID projectId, String owner) {
        final ProjectEntity projectEntity = projectsService.getProject(projectId, owner);
        return projectInformationMapper.projectEntityToProjectInformation(projectEntity);
    }

    public Page<ProjectInformationModel> getAll(String owner, Pageable pageable) {
        final Page<ProjectEntity> projectEntityPage = projectsService.getAll(owner, pageable);
        final List<ProjectInformationModel> projectInformation = projectEntityPage.stream()
                .map(projectInformationMapper::projectEntityToProjectInformation)
                .toList();
        return new PageImpl<>(projectInformation, projectEntityPage.getPageable(), projectEntityPage.getTotalElements());
    }

}
