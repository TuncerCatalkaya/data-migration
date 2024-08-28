package org.datamigration.mapper;

import org.datamigration.domain.model.ProjectModel;
import org.datamigration.jpa.entity.ProjectEntity;
import org.datamigration.usecase.model.ProjectInformationModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectInformationMapper {

    ProjectModel projectInformationToProject(ProjectInformationModel projectInformation);
    ProjectInformationModel projectToProjectInformation(ProjectModel project);

    ProjectEntity projectInformationToProjectEntity(ProjectInformationModel projectInformation);
    ProjectInformationModel projectEntityToProjectInformation(ProjectEntity projectEntity);

}
