package org.datamigration.mapper;

import org.datamigration.domain.model.ProjectModel;
import org.datamigration.jpa.entity.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = ScopeMapper.class)
public interface ProjectMapper {

    ProjectModel projectEntityToProject(ProjectEntity projectEntity);

    ProjectEntity projectToProjectEntity(ProjectModel project);

}
