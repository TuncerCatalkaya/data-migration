package org.datamigration.jpa.repository;

import jakarta.transaction.Transactional;
import org.datamigration.jpa.entity.CheckpointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaCheckpointRepository extends JpaRepository<CheckpointEntity, UUID> {

    Optional<CheckpointEntity> findByScope_Id(@Param("scopeId") UUID scopeID);

    @Modifying
    @Transactional
    void deleteByScope_Id(UUID scopeId);
}
