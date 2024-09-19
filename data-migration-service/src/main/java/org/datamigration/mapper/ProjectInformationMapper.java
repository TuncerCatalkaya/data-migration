package org.datamigration.mapper;

import org.datamigration.jpa.entity.ProjectEntity;
import org.datamigration.usecase.model.ProjectInformationModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectInformationMapper {

    ProjectInformationModel projectEntityToProjectInformation(ProjectEntity projectEntity);

}
