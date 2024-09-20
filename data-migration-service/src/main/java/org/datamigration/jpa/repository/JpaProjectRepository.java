package org.datamigration.jpa.repository;

import org.datamigration.jpa.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    boolean existsByIdAndOwner(UUID id, String owner);

    Optional<ProjectEntity> findByIdAndOwner(UUID id, String owner);

    Page<ProjectEntity> findAllByOwner(String owner, Pageable pageable);

}
