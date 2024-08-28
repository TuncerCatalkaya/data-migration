package org.datamigration.domain.repository;

import java.util.List;
import java.util.UUID;

public interface ItemRepository {
    void deleteAllById(List<UUID> itemIds);
}
