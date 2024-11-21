package org.datamigration.mapper;

import org.datamigration.jpa.entity.ProjectEntity;
import org.datamigration.model.ProjectModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {
    ProjectModel projectEntityToProject(ProjectEntity projectEntity);
}
