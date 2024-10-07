package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.exception.ScopeNotFinishedException;
import org.datamigration.jpa.entity.ItemEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.mapper.ItemMapper;
import org.datamigration.model.ItemModel;
import org.datamigration.service.ItemsService;
import org.datamigration.service.MappedItemsService;
import org.datamigration.service.ProjectsService;
import org.datamigration.service.ScopesService;
import org.datamigration.usecase.api.ItemsMethods;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
class Items implements ItemsMethods {

    private final ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);
    private final ProjectsService projectsService;
    private final ScopesService scopesService;
    private final ItemsService itemsService;
    private final MappedItemsService mappedItemsService;

    public ItemModel updateItemProperty(UUID projectId, UUID itemId, String key, String newValue, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        final ItemEntity itemEntity = itemsService.updateItemProperty(itemId, key, newValue);
        return itemMapper.itemEntityToItem(itemEntity);
    }

    public Page<ItemModel> getAllItems(UUID projectId, UUID scopeId, String createdBy, Pageable pageable) {
        projectsService.isPermitted(projectId, createdBy);
        final ScopeEntity scopeEntity = scopesService.get(scopeId);
        if (!scopeEntity.isFinished()) {
            throw new ScopeNotFinishedException("Scope with id " + scopeId + " is not finished with import process.");
        }
        final Page<ItemEntity> itemEntityPage = itemsService.getAll(scopeId, pageable);

        final Map<UUID, List<UUID>> itemToMappingsMap =
                mappedItemsService.getItemWithMappings(itemEntityPage.stream()
                        .map(ItemEntity::getId)
                        .toList());

        final List<ItemModel> items = itemEntityPage.stream()
                .map(itemMapper::itemEntityToItem)
                .map(item -> {
                    final List<UUID> mappingIds = itemToMappingsMap.get(item.getId());
                    item.setMappingIds(mappingIds);
                    return item;
                })
                .toList();
        return new PageImpl<>(items, itemEntityPage.getPageable(), itemEntityPage.getTotalElements());
    }

}
