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
import org.datamigration.usecase.model.CreateMappingsRequestModel;
import org.mapstruct.factory.Mappers;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
class Mappings implements MappingsMethods {

    private final MappingMapper mappingMapper = Mappers.getMapper(MappingMapper.class);
    private final ProjectsService projectsService;
    private final ScopesService scopesService;
    private final MappingsService mappingsService;
    private final HostsService hostsService;

    public MappingModel createMapping(UUID projectId, UUID scopeId, CreateMappingsRequestModel createMappingsRequest, String owner) {
        projectsService.isPermitted(projectId, owner);
        final ScopeEntity scopeEntity = scopesService.get(scopeId);
        final HostEntity hostEntity = hostsService.get(createMappingsRequest.getHostId());
        final MappingEntity mappingEntity = new MappingEntity();
        mappingEntity.setName(createMappingsRequest.getMappingName());
        mappingEntity.setHost(hostEntity);
        mappingEntity.setScope(scopeEntity);
        return Optional.of(mappingEntity)
                .map(mapping -> mappingsService.createNewMapping(mapping, scopeEntity.getHeaders()))
                .map(mappingMapper::mappingEntityToMapping)
                .orElse(null);
    }

}
