package org.datamigration.service;

import lombok.RequiredArgsConstructor;
import org.datamigration.exception.ProjectForbiddenException;
import org.datamigration.exception.ProjectNotFoundException;
import org.datamigration.jpa.entity.ProjectEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.jpa.repository.JpaProjectRepository;
import org.datamigration.jpa.repository.JpaScopeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectsService {

    private final JpaProjectRepository jpaProjectRepository;
    private final JpaScopeRepository jpaScopeRepository;

    public ProjectEntity createOrUpdateProject(ProjectEntity projectEntity) {
        return jpaProjectRepository.save(projectEntity);
    }

    public ScopeEntity createOrGetScope(UUID projectId, String scopeKey, boolean external) {
        final ProjectEntity projectEntity = getProject(projectId);
        return jpaScopeRepository.findByProject_IdAndKey(projectId, scopeKey)
                .orElseGet(() -> {
                    final ScopeEntity scopeEntity = new ScopeEntity();
                    scopeEntity.setKey(scopeKey);
                    scopeEntity.setCreatedDate(new Date());
                    scopeEntity.setExternal(external);
                    scopeEntity.setFinished(false);
                    scopeEntity.setProject(projectEntity);
                    return jpaScopeRepository.save(scopeEntity);
                });
    }

    public ProjectEntity getProject(UUID projectId) {
        return jpaProjectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project with id " + projectId + " not found."));
    }

    public ProjectEntity getProject(UUID projectId, String owner) {
        return jpaProjectRepository.findByIdAndOwner(projectId, owner)
                .orElseThrow(() -> new ProjectNotFoundException("Project with id " + projectId + " not found."));
    }

    public void isPermitted(UUID projectId, String owner) {
        final boolean isPermitted = jpaProjectRepository.existsByIdAndOwner(projectId, owner);
        if (!isPermitted) {
            throw new ProjectForbiddenException("Forbidden to access project with id " + projectId + ".");
        }
    }

    public Page<ProjectEntity> getAll(String owner, Pageable pageable) {
        final Pageable pageRequest =
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSortOr(
                        Sort.by(Sort.Direction.DESC, "lastUpdatedDate")));
        return jpaProjectRepository.findAllByOwner(owner, pageRequest);
    }

}
