package org.datamigration.jpa.repository;

import org.datamigration.jpa.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    boolean existsByIdAndCreatedBy(UUID projectID, String createdBy);

    Optional<ProjectEntity> findByIdAndCreatedBy(UUID projectId, String createdBy);

    Page<ProjectEntity> findAllByCreatedBy(String createdBy, Pageable pageable);

}
