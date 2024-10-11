package org.datamigration.usecase.api;

import org.datamigration.model.MappedItemModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MappedItemsMethods {
    Page<MappedItemModel> getAllMappedItems(UUID projectId, UUID mappingId, String createdBy, Pageable pageable);
    MappedItemModel updateMappedItemProperty(UUID projectId, UUID mappedItemId, String key, String newValue, String createdBy);
}
