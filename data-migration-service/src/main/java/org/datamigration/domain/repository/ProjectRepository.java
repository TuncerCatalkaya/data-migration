package org.datamigration.domain.repository;

import org.datamigration.domain.model.ProjectModel;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository {
    ProjectModel save(ProjectModel project);
    Optional<ProjectModel> findById(UUID id);
}
