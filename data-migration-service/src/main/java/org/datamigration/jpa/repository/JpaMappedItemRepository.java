package org.datamigration.jpa.repository;

import org.datamigration.jpa.entity.MappedItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaMappedItemRepository extends JpaRepository<MappedItemEntity, UUID> {
    List<MappedItemEntity> findAllByItem_Id(UUID itemId);
    Page<MappedItemEntity> findAllByMapping_Id(UUID mappingId, Pageable pageable);
}
