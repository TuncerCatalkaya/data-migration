package org.datamigration.usecase.api;

import org.datamigration.model.ItemModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ItemsMethods {
    ItemModel updateItemProperty(UUID projectId, UUID itemId, String key, String newValue, String owner);

    Page<ItemModel> getAllItems(UUID projectId, UUID scopeId, String owner, Pageable pageable);
}
