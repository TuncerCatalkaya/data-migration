package org.datamigration.jpa.repository;

import org.datamigration.jpa.entity.CheckpointBatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JpaCheckpointBatchRepository extends JpaRepository<CheckpointBatchEntity, UUID> {
    boolean existsByCheckpoint_ScopeIdAndBatchIndex(UUID scopeId, Long batchIndex);

    @Query("""
        SELECT COUNT(cb.batchIndex) FROM CheckpointBatchEntity cb
        JOIN cb.checkpoint cp
        WHERE cp.scope.id = :scopeId
    """)
    long countBatchIndexByScopeId(@Param("scopeId") UUID scopeId);

}
