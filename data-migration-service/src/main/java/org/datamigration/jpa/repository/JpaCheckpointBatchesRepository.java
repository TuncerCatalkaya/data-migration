package org.datamigration.jpa.repository;

import org.datamigration.jpa.entity.CheckpointBatchesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaCheckpointBatchesRepository extends JpaRepository<CheckpointBatchesEntity, UUID> {
    boolean existsByCheckpoint_ScopeIdAndBatchIndex(UUID scopeId, Long batchIndex);
}
