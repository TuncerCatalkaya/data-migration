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

    @Query(value = """
            SELECT * FROM item i
            WHERE i.scope_id = :scopeId
            """, nativeQuery = true)
    Page<ItemEntity> findAllByScopeId(UUID scopeId, Pageable pageable);

    @Query(value = """
        SELECT * FROM item i
        WHERE i.scope_id = :scopeId
        AND (:headerName IS NULL OR :value IS NULL OR i.properties -> :headerName ->> 'value' ILIKE CONCAT(:value, '%'))
        """, nativeQuery = true)
    Page<ItemEntity> findAllByScopeIdAndDynamicHeader(
            @Param("scopeId") UUID scopeId,
            @Param("headerName") String headerName,
            @Param("value") String value,
            Pageable pageable
    );

    @Query(value = """
            SELECT * FROM item i
            WHERE i.scope_id = :scopeId
            AND (
                :value IS NULL 
                OR EXISTS (
                    SELECT 1
                    FROM jsonb_each(i.properties) AS prop(key, val)
                    WHERE val->>'value' ILIKE CONCAT(:value, '%')
                )
            )
            """, nativeQuery = true)
    Page<ItemEntity> findAllByScopeIdWithFreeTextSearch(
            @Param("scopeId") UUID scopeId,
            @Param("value") String value,
            Pageable pageable
    );

    @Query(value = """
            SELECT * FROM item i
            WHERE i.scope_id = :scopeId
            AND (:mappingId IS NULL OR NOT EXISTS (
                SELECT 1 FROM mapped_item mi
                WHERE mi.item_id = i.id AND mi.mapping_id = :mappingId
            ))
            """, nativeQuery = true)
    Page<ItemEntity> findAllByScopeIdAndMappingIdNotInMappedItems(@Param("scopeId") UUID scopeId,
                                                                  @Param("mappingId") UUID mappingId, Pageable pageable);

    @Query(value = """
            SELECT * FROM item i
            WHERE i.scope_id = :scopeId
            AND (:headerName IS NULL OR :value IS NULL OR i.properties -> :headerName ->> 'value' ILIKE CONCAT(:value, '%'))
            AND (:mappingId IS NULL OR NOT EXISTS (
                SELECT 1 FROM mapped_item mi
                WHERE mi.item_id = i.id AND mi.mapping_id = :mappingId
            ))
            """, nativeQuery = true)
    Page<ItemEntity> findAllByScopeIdAndMappingIdNotInMappedItemsAndDynamicHeader(
            @Param("scopeId") UUID scopeId,
            @Param("mappingId") UUID mappingId,
            @Param("headerName") String headerName,
            @Param("value") String value,
            Pageable pageable
    );

    @Query(value = """
            SELECT * FROM item i
            WHERE i.scope_id = :scopeId
            AND (
                :value IS NULL 
                OR EXISTS (
                    SELECT 1
                    FROM jsonb_each(i.properties) AS prop(key, val)
                    WHERE val->>'value' ILIKE CONCAT(:value, '%')
                )
            )
            AND (:mappingId IS NULL OR NOT EXISTS (
                SELECT 1 FROM mapped_item mi
                WHERE mi.item_id = i.id AND mi.mapping_id = :mappingId
            ))
            """, nativeQuery = true)
    Page<ItemEntity> findAllByScopeIdAndMappingIdNotInMappedItemsWithFreeTextSearch(
            @Param("scopeId") UUID scopeId,
            @Param("mappingId") UUID mappingId,
            @Param("value") String value,
            Pageable pageable
    );

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
