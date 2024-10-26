package org.datamigration.jpa.repository;

import org.datamigration.jpa.entity.ItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    // 6832711c-0d0a-4b01-b4ec-6369561910ff
    @Query(value = """
        SELECT MIN(item_id::text) AS id
        FROM (
            SELECT item.id as item_id, string_agg(LOWER(value ->> 'value'), ', ' ORDER BY LOWER(value ->> 'value')) AS combined_values
            FROM item, jsonb_each(properties)
            WHERE scope_id = :scopeId
            GROUP BY item.id
        )
        GROUP BY combined_values
        HAVING COUNT(*) > 1;
    """, nativeQuery = true)
    List<String> findDuplicateItemIds(@Param("scopeId") String scopeId);

}
