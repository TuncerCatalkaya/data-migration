package org.datamigration.usecase.api;

import org.datamigration.model.MappingModel;
import org.datamigration.usecase.model.CreateOrUpdateMappingsRequestModel;

import java.util.List;
import java.util.UUID;

public interface MappingsMethods {
    MappingModel createOrUpdateMapping(UUID projectId, UUID scopeId, CreateOrUpdateMappingsRequestModel createMappingsRequest, String owner);
    List<MappingModel> getAllMappings(UUID projectId, UUID scopeId, String owner);
    void markMappingForDeletion(UUID projectId, UUID mappingId, String owner);
}
