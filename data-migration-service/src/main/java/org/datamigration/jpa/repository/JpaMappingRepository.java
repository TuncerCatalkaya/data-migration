package org.datamigration.jpa.repository;

import jakarta.transaction.Transactional;
import org.datamigration.jpa.entity.MappingEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaMappingRepository extends JpaRepository<MappingEntity, UUID> {
    List<MappingEntity> findAllByScope_IdAndScope_DeleteFalse(UUID scopeId, Sort sort);

    @Query("""
        SELECT m FROM MappingEntity m
        JOIN FETCH m.database
        WHERE m.scope.id = :scopeId AND m.scope.delete = false
    """)
    List<MappingEntity> findAllMappings(@Param("scopeId") UUID scopeId, Sort sort);

    @Modifying
    @Transactional
    @Query("""
        UPDATE MappingEntity
        SET delete = true
        WHERE id = :mappingId
    """)
    void markForDeletion(@Param("mappingId") UUID mappingId);
}
