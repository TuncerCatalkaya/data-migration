package org.datamigration.jpa.repository;

import org.datamigration.jpa.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaItemRepository extends JpaRepository<ItemEntity, UUID> {

}
