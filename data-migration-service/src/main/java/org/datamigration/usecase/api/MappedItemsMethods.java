package org.datamigration.usecase.api;

import org.datamigration.model.MappedItemModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MappedItemsMethods {
    Page<MappedItemModel> getAllMappedItemsByMapping(UUID projectId, UUID mappingId, String createdBy, Pageable pageable);
}
