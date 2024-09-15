package org.datamigration.jpa.repository;

import org.datamigration.jpa.entity.CheckpointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaCheckpointRepository extends JpaRepository<CheckpointEntity, UUID> {

}
