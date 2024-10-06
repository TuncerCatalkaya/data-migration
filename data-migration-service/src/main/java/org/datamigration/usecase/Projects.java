package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.ProjectEntity;
import org.datamigration.mapper.ProjectMapper;
import org.datamigration.model.ProjectModel;
import org.datamigration.service.ProjectsService;
import org.datamigration.usecase.api.ProjectsMethods;
import org.datamigration.usecase.model.CreateProjectsRequestModel;
import org.datamigration.usecase.model.UpdateProjectsRequestModel;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
class Projects implements ProjectsMethods {

    private final ProjectMapper projectMapper = Mappers.getMapper(ProjectMapper.class);
    private final ProjectsService projectsService;

    public ProjectModel createNewProject(CreateProjectsRequestModel createProjectsRequest) {
        final ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName(createProjectsRequest.getProjectName());
        return Optional.of(projectEntity)
                .map(projectsService::createOrUpdateProject)
                .map(projectMapper::projectEntityToProject)
                .orElse(null);
    }

    public ProjectModel updateProject(UpdateProjectsRequestModel updateProjectsRequest, String createdBy) {
        final ProjectEntity projectEntity = projectsService.getProject(updateProjectsRequest.getProjectId(), createdBy);
        projectEntity.setName(updateProjectsRequest.getProjectName());
        return Optional.of(projectEntity)
                .map(projectsService::createOrUpdateProject)
                .map(projectMapper::projectEntityToProject)
                .orElse(null);
    }

    @Override
    public void isProjectPermitted(UUID projectId, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
    }

    public ProjectModel getProject(UUID projectId, String createdBy) {
        final ProjectEntity projectEntity = projectsService.getProject(projectId, createdBy);
        return projectMapper.projectEntityToProject(projectEntity);
    }

    public Page<ProjectModel> getAllProjects(String createdBy, Pageable pageable) {
        final Page<ProjectEntity> projectEntityPage = projectsService.getAll(createdBy, pageable);
        final List<ProjectModel> project = projectEntityPage.stream()
                .map(projectMapper::projectEntityToProject)
                .toList();
        return new PageImpl<>(project, projectEntityPage.getPageable(), projectEntityPage.getTotalElements());
    }
}
