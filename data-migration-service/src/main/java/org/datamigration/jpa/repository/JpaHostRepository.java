package org.datamigration.jpa.repository;

import org.datamigration.jpa.entity.HostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaHostRepository extends JpaRepository<HostEntity, UUID> {

}
