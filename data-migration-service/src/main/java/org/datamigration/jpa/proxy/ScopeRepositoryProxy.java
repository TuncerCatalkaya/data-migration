package org.datamigration.jpa.proxy;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.datamigration.domain.model.ProjectModel;
import org.datamigration.domain.model.ScopeModel;
import org.datamigration.domain.repository.ScopeRepository;
import org.datamigration.jpa.entity.ProjectEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.jpa.repository.JpaScopeRepository;
import org.datamigration.mapper.ProjectMapper;
import org.datamigration.mapper.ScopeMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScopeRepositoryProxy implements ScopeRepository {

    private final ProjectMapper projectMapper = Mappers.getMapper(ProjectMapper.class);
    private final ScopeMapper scopeMapper = Mappers.getMapper(ScopeMapper.class);
    private final JpaScopeRepository jpaScopeRepository;

    @Transactional
    @Override
    public ScopeModel save(ProjectModel project, ScopeModel scope) {
        final ProjectEntity projectEntity = projectMapper.projectToProjectEntity(project);
        final ScopeEntity scopeEntity = scopeMapper.scopeToScopeEntity(scope);
        scopeEntity.setProject(projectEntity);
        scopeEntity.setKey(scope.getKey());
        projectEntity.getScopes().add(scopeEntity);
        final ScopeEntity savedScopeEntity = jpaScopeRepository.save(scopeEntity);
        return scopeMapper.scopeEntityToScope(savedScopeEntity);
    }

}
