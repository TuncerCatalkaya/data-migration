package org.datamigration.jpa.repository;

import org.datamigration.jpa.entity.MappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaMappingRepository extends JpaRepository<MappingEntity, UUID> {

}
