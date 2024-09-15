package org.datamigration.domain.repository;

import org.datamigration.domain.model.ProjectModel;
import org.datamigration.domain.model.ScopeModel;
import org.springframework.stereotype.Repository;

@Repository
public interface ScopeRepository {
    ScopeModel save(ProjectModel project, ScopeModel scope);
}
