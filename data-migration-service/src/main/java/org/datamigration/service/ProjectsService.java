package org.datamigration.service;

import lombok.RequiredArgsConstructor;
import org.datamigration.exception.ProjectForbiddenException;
import org.datamigration.exception.ProjectNotFoundException;
import org.datamigration.jpa.entity.ProjectEntity;
import org.datamigration.jpa.repository.JpaProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ProjectsService {

    private final JpaProjectRepository jpaProjectRepository;

    public ProjectEntity createOrUpdateProject(ProjectEntity projectEntity) {
        return jpaProjectRepository.save(projectEntity);
    }

    public ProjectEntity getProject(UUID projectId) {
        return jpaProjectRepository.findById(projectId)
                .orElseThrow(getProjectNotFoundException(projectId));
    }

    public ProjectEntity getProject(UUID projectId, String createdBy) {
        return jpaProjectRepository.findByIdAndCreatedBy(projectId, createdBy)
                .orElseThrow(getProjectNotFoundException(projectId));
    }

    public void isPermitted(UUID projectId, String createdBy) {
        if (projectId == null) {
            return;
        }
        final boolean isPermitted = jpaProjectRepository.existsByIdAndCreatedBy(projectId, createdBy);
        if (!isPermitted) {
            throw new ProjectForbiddenException("Forbidden to access project with id " + projectId + ".");
        }
    }

    public Page<ProjectEntity> getAll(String createdBy, Pageable pageable) {
        final Pageable pageRequest =
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSortOr(
                        Sort.by(Sort.Direction.DESC, "lastModifiedDate")));
        return jpaProjectRepository.findAllByCreatedBy(createdBy, pageRequest);
    }

    private Supplier<ProjectNotFoundException> getProjectNotFoundException(UUID projectId) {
        return () -> new ProjectNotFoundException("Project with id " + projectId + " not found.");
    }
}
