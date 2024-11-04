package org.datamigration.usecase.api;

import org.datamigration.model.ItemModel;
import org.datamigration.usecase.model.UpdateItemPropertiesRequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ItemsMethods {
    ItemModel updateItemProperty(UUID projectId, UUID itemId, String key, String newValue, String createdBy);

    void updateItemProperties(UUID projectId, UpdateItemPropertiesRequestModel updateItemPropertiesRequest, String key,
                              String newValue, String createdBy);

    Page<ItemModel> getAllItems(UUID projectId, UUID scopeId, UUID mappingId, boolean filterMappedItems, String header,
                                String search, String createdBy, Pageable pageable);
}
