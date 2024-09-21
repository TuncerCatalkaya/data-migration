package org.datamigration.jpa.repository;

import jakarta.transaction.Transactional;
import org.datamigration.jpa.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public interface JpaProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    boolean existsByIdAndOwner(UUID id, String owner);

    Optional<ProjectEntity> findByIdAndOwner(UUID id, String owner);

    Page<ProjectEntity> findAllByOwner(String owner, Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
        UPDATE ProjectEntity
        SET name = :name, lastUpdatedDate = :lastUpdatedDate
        WHERE id = :projectId
    """)
    void updateProject(@Param("projectId") UUID projectId, @Param("name") String name,
                       @Param("lastUpdatedDate") Date lastUpdatedDate);

}
