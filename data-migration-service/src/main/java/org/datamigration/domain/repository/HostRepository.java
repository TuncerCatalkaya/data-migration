package org.datamigration.domain.repository;

import org.datamigration.domain.model.HostModel;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HostRepository {
    HostModel save(HostModel host);
    void deleteById(UUID id);
}
