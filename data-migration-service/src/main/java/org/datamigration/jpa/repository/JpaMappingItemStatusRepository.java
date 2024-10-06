package org.datamigration.jpa.repository;

import org.datamigration.jpa.entity.MappingItemStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaMappingItemStatusRepository extends JpaRepository<MappingItemStatusEntity, UUID> {
    List<MappingItemStatusEntity> findAllByItem_Id(UUID itemId);
}
