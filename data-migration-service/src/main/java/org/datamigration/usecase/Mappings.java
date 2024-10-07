package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.exception.MappingValidationException;
import org.datamigration.jpa.entity.DatabaseEntity;
import org.datamigration.jpa.entity.ItemEntity;
import org.datamigration.jpa.entity.MappedItemEntity;
import org.datamigration.jpa.entity.MappingEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.mapper.MappingMapper;
import org.datamigration.model.ItemStatusModel;
import org.datamigration.model.MappingModel;
import org.datamigration.service.HostsService;
import org.datamigration.service.ItemsService;
import org.datamigration.service.MappedItemsService;
import org.datamigration.service.MappingsService;
import org.datamigration.service.ProjectsService;
import org.datamigration.service.ScopesService;
import org.datamigration.usecase.api.MappingsMethods;
import org.datamigration.usecase.model.ApplyMappingRequestModel;
import org.datamigration.usecase.model.CreateOrUpdateMappingsRequestModel;
import org.mapstruct.factory.Mappers;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
class Mappings implements MappingsMethods {

    private final MappingMapper mappingMapper = Mappers.getMapper(MappingMapper.class);
    private final ProjectsService projectsService;
    private final ScopesService scopesService;
    private final ItemsService itemsService;
    private final MappingsService mappingsService;
    private final MappedItemsService mappingItemService;
    private final HostsService hostsService;

    public MappingModel createOrUpdateMapping(UUID projectId, UUID scopeId,
                                              CreateOrUpdateMappingsRequestModel createOrUpdateMappingsRequest,
                                              String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        final ScopeEntity scopeEntity = scopesService.getAndCheckIfScopeFinished(scopeId);
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
                .map(mappingMapper::mappingEntityToMapping)
                .orElse(null);
    }

    public void applyMapping(UUID projectId, ApplyMappingRequestModel applyMappingRequest, String createdBy) {
        try {
            projectsService.isPermitted(projectId, createdBy);
            final MappingEntity mappingEntity = mappingsService.get(applyMappingRequest.getMappingId());
            final List<ItemEntity> itemEntities = itemsService.getAll(applyMappingRequest.getItemIds());

            final UUID scopeId = mappingEntity.getScope().getId();
            scopesService.getAndCheckIfScopeFinished(scopeId);
            final boolean scopeIdDoesNotMatch = itemEntities.stream()
                    .anyMatch(itemEntity -> !itemEntity.getScope().getId().equals(scopeId));
            if (scopeIdDoesNotMatch) {
                throw new MappingValidationException(
                        "Items are not valid, because at least one of the items has a different scope than the scope of the specified mapping.");
            }


            final List<MappedItemEntity> mappedItemEntities = itemEntities.stream()
                    .map(itemEntity -> {
                        final MappedItemEntity mappedItemEntity = new MappedItemEntity();
                        mappedItemEntity.setMapping(mappingEntity);
                        mappedItemEntity.setItem(itemEntity);
                        mappedItemEntity.setStatus(ItemStatusModel.MAPPED);
                        return mappedItemEntity;
                    })
                    .toList();
            mappingItemService.applyMapping(mappedItemEntities);
        } catch (DataIntegrityViolationException ex) {
            throw new MappingValidationException(ex);
        }
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
