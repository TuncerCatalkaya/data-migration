package org.datamigration.jpa.repository;

import org.datamigration.jpa.entity.ItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaItemRepository extends JpaRepository<ItemEntity, UUID> {
    Page<ItemEntity> findAllByScope_Id(UUID scopeId, Pageable pageable);
}
