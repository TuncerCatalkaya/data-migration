package org.datamigration.jpa.proxy;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.datamigration.domain.model.ProjectModel;
import org.datamigration.domain.repository.ProjectRepository;
import org.datamigration.jpa.repository.JpaProjectRepository;
import org.datamigration.mapper.ProjectMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProjectRepositoryProxy implements ProjectRepository {

    private final ProjectMapper projectMapper = Mappers.getMapper(ProjectMapper.class);
    private final JpaProjectRepository jpaProjectRepository;

    @Transactional
    @Override
    public ProjectModel save(ProjectModel project) {
        return Optional.of(project)
                .map(projectMapper::projectToProjectEntity)
                .map(jpaProjectRepository::save)
                .map(projectMapper::projectEntityToProject)
                .orElse(null);
    }

    @Transactional
    @Override
    public Optional<ProjectModel> findById(UUID id) {
        return jpaProjectRepository.findById(id)
                .map(projectMapper::projectEntityToProject);
    }

}
