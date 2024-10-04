package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.HostEntity;
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
                                              CreateOrUpdateMappingsRequestModel createOrUpdateMappingsRequest, String owner) {
        projectsService.isPermitted(projectId, owner);
        final ScopeEntity scopeEntity = scopesService.get(scopeId);
        final UUID mappingId = createOrUpdateMappingsRequest.getMappingId();
        final Map<String, String[]> mapping = createOrUpdateMappingsRequest.getMapping();
        mappingsService.validateMapping(mappingId, mapping, scopeEntity.getHeaders());
        final HostEntity hostEntity = hostsService.get(createOrUpdateMappingsRequest.getHostId());
        final MappingEntity mappingEntity = new MappingEntity();
        mappingEntity.setId(mappingId);
        mappingEntity.setName(createOrUpdateMappingsRequest.getMappingName());
        mappingEntity.setMapping(mapping);
        mappingEntity.setHost(hostEntity);
        mappingEntity.setScope(scopeEntity);
        return Optional.of(mappingEntity)
                .map(mappingsService::createNewMapping)
                .map(mappingMapper::mappingEntityToMapping)
                .orElse(null);
    }

    public List<MappingModel> getAllMappings(UUID projectId, UUID scopeId, String owner) {
        projectsService.isPermitted(projectId, owner);
        return mappingsService.getAll(scopeId).stream()
                .map(mappingMapper::mappingEntityToMapping)
                .toList();
    }

    public void markMappingForDeletion(UUID projectId, UUID mappingId, String owner) {
        projectsService.isPermitted(projectId, owner);
        mappingsService.markForDeletion(mappingId);
    }

}
