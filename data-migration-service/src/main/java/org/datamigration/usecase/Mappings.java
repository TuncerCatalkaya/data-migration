package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.DatabaseEntity;
import org.datamigration.jpa.entity.MappingEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.mapper.MappingMapper;
import org.datamigration.model.MappingModel;
import org.datamigration.service.HostsService;
import org.datamigration.service.MappingsService;
import org.datamigration.service.ProjectsService;
import org.datamigration.service.ScopesService;
import org.datamigration.usecase.api.MappingsMethods;
import org.datamigration.usecase.model.CreateOrUpdateMappingsRequestModel;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
class Mappings implements MappingsMethods {

    private final MappingMapper mappingMapper = Mappers.getMapper(MappingMapper.class);
    private final ProjectsService projectsService;
    private final ScopesService scopesService;
    private final MappingsService mappingsService;
    private final HostsService hostsService;

    public MappingModel createOrUpdateMapping(UUID projectId, UUID scopeId,
                                              CreateOrUpdateMappingsRequestModel createOrUpdateMappingsRequest, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        final ScopeEntity scopeEntity = scopesService.get(scopeId);
        final UUID mappingId = createOrUpdateMappingsRequest.getMappingId();
        final Map<String, String[]> mapping = createOrUpdateMappingsRequest.getMapping();
        mappingsService.validateMapping(mappingId, mapping, scopeEntity.getHeaders());
        final DatabaseEntity databaseEntity = hostsService.getDatabase(createOrUpdateMappingsRequest.getDatabaseId());
        final MappingEntity mappingEntity = (mappingId != null) ? mappingsService.get(mappingId) : getNewMappingEntity();
        mappingEntity.setId(mappingId);
        mappingEntity.setName(createOrUpdateMappingsRequest.getMappingName());
        mappingEntity.setMapping(mapping);
        mappingEntity.setDatabase(databaseEntity);
        mappingEntity.setScope(scopeEntity);
        return Optional.of(mappingEntity)
                .map(mappingsService::createOrUpdateMapping)
                .map(m -> {
                    m.setDatabase(databaseEntity);
                    return m;
                })
                .map(mappingMapper::mappingEntityToMapping)
                .orElse(null);
    }

    public List<MappingModel> getAllMappings(UUID projectId, UUID scopeId, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        return mappingsService.getAll(scopeId).stream()
                .map(mappingMapper::mappingEntityToMapping)
                .toList();
    }

    public void markMappingForDeletion(UUID projectId, UUID mappingId, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        mappingsService.markForDeletion(mappingId);
    }

    private MappingEntity getNewMappingEntity() {
        final MappingEntity mappingEntity = new MappingEntity();
        mappingEntity.setFinished(false);
        mappingEntity.setLocked(false);
        mappingEntity.setDelete(false);
        mappingEntity.setLastProcessedBatch(-1);
        return mappingEntity;
    }

}
