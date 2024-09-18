package org.datamigration.jpa.repository;

import jakarta.transaction.Transactional;
import org.datamigration.jpa.entity.CheckpointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaCheckpointRepository extends JpaRepository<CheckpointEntity, UUID> {

    Optional<CheckpointEntity> findByScope_Id(UUID scopeID);

    boolean existsByScope_Id(UUID scopeId);

    @Query("""
        SELECT c.batchSize FROM CheckpointEntity c
        WHERE c.scope.id = :scopeId
    """)
    int findBatchSizeByScopeId(@Param("scopeId") UUID scopeId);

    @Query("""
        SELECT c.totalBatches FROM CheckpointEntity c
        WHERE c.scope.id = :scopeId
    """)
    long findTotalBatchesByScopeId(@Param("scopeId") UUID scopeId);

    @Modifying
    @Transactional
    void deleteByScope_Id(UUID scopeId);
}
