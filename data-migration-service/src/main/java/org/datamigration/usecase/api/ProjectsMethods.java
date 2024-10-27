package org.datamigration.usecase.api;

import org.datamigration.model.ProjectModel;
import org.datamigration.usecase.model.CreateProjectsRequestModel;
import org.datamigration.usecase.model.UpdateProjectsRequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProjectsMethods {
    ProjectModel createNewProject(CreateProjectsRequestModel createProjectsRequest);
    ProjectModel updateProject(UpdateProjectsRequestModel updateProjectsRequest, String createdBy);
    void isProjectPermitted(UUID projectId, String createdBy);
    ProjectModel getProject(UUID projectId, String createdBy);
    Page<ProjectModel> getAllProjects(String createdBy, Pageable pageable);
    void markProjectForDeletion(UUID projectId, String createdBy);
}
