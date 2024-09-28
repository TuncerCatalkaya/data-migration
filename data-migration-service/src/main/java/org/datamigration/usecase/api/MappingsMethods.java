package org.datamigration.usecase.api;

import org.datamigration.model.MappingModel;
import org.datamigration.usecase.model.CreateMappingsRequestModel;

import java.util.UUID;

public interface MappingsMethods {
    MappingModel createMapping(UUID projectId, UUID scopeId, CreateMappingsRequestModel createMappingsRequest, String owner);
}
