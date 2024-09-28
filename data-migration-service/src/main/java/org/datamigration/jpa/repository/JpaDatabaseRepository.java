package org.datamigration.jpa.repository;

import org.datamigration.jpa.entity.DatabaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface JpaDatabaseRepository extends JpaRepository<DatabaseEntity, UUID> {
    Set<DatabaseEntity> findAllByHost_Id(UUID hostId);
}
