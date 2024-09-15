package org.datamigration.usecase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.datamigration.domain.model.ProjectModel;
import org.datamigration.domain.model.ScopeModel;
import org.datamigration.domain.service.DataMigrationService;
import org.datamigration.jpa.entity.ProjectEntity;
import org.datamigration.jpa.repository.JpaProjectRepository;
import org.datamigration.mapper.ProjectInformationMapper;
import org.datamigration.usecase.model.CreateProjectsRequestModel;
import org.datamigration.usecase.model.ProjectInformationModel;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
@Component
@RequiredArgsConstructor
public class ProjectsUsecase {

    private final ProjectInformationMapper projectInformationMapper = Mappers.getMapper(ProjectInformationMapper.class);
    private final DataMigrationService dataMigrationService;
    private final JpaProjectRepository jpaProjectRepository;

    public boolean isPermitted(UUID projectId, String owner) {
        return jpaProjectRepository.existsByIdAndOwner(projectId, owner);
    }

    public ProjectInformationModel createNew(CreateProjectsRequestModel createProjectsRequest, String owner) {
        return Optional.of(ProjectInformationModel.builder()
                        .name(createProjectsRequest.getProjectName())
                        .owner(owner)
                        .build())
                .map(projectInformationMapper::projectInformationToProject)
                .map(dataMigrationService::createOrUpdateProject)
                .map(projectInformationMapper::projectToProjectInformation)
                .orElse(null);
    }

    public ScopeModel addInputScope(UUID projectId, String scope) {
        return dataMigrationService.addInputScope(projectId, scope);
    }

    public Page<ProjectInformationModel> getAll(Pageable pageable) {
        final Pageable pageRequest =
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSortOr(Sort.by(Sort.Direction.DESC, "lastUpdatedDate")));
        final Page<ProjectEntity> projectEntityPage = jpaProjectRepository.findAll(pageRequest);
        final List<ProjectInformationModel> projectInformation = projectEntityPage.stream()
                .map(projectInformationMapper::projectEntityToProjectInformation)
                .toList();
        return new PageImpl<>(projectInformation, projectEntityPage.getPageable(), projectEntityPage.getTotalElements());
    }

    public ProjectModel get(UUID projectId) {
        return dataMigrationService.findProject(projectId);
    }

}
