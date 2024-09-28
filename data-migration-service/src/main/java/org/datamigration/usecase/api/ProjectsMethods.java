package org.datamigration.usecase.api;

import org.datamigration.model.ProjectModel;
import org.datamigration.usecase.model.CreateProjectsRequestModel;
import org.datamigration.usecase.model.UpdateProjectsRequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProjectsMethods {
    ProjectModel createNewProject(CreateProjectsRequestModel createProjectsRequest, String owner);
    ProjectModel updateProject(UpdateProjectsRequestModel updateProjectsRequest, String owner);
    void isProjectPermitted(UUID projectId, String owner);
    ProjectModel getProject(UUID projectId, String owner);
    Page<ProjectModel> getAllProjects(String owner, Pageable pageable);
}
