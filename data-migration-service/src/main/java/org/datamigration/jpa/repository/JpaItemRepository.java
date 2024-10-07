package org.datamigration.jpa.repository;

import org.datamigration.jpa.entity.ItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JpaItemRepository extends JpaRepository<ItemEntity, UUID> {
    Page<ItemEntity> findAllByScope_Id(UUID scopeId, Pageable pageable);

    @Query("""
            SELECT i FROM ItemEntity i
            WHERE i.scope.id = :scopeId
            AND NOT EXISTS (
                SELECT mi FROM MappedItemEntity mi
                WHERE mi.item = i AND mi.mapping.id = :mappingId
            )
    """)
    Page<ItemEntity> findAllByScopeIdAndMappingIdNotInMappedItems(@Param("scopeId") UUID scopeId,
                                                                  @Param("mappingId") UUID mappingId, Pageable pageable);
}
