package org.dataintegration.usecase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dataintegration.jpa.entity.ItemEntity;
import org.dataintegration.mapper.ItemMapper;
import org.dataintegration.model.ItemModel;
import org.dataintegration.model.ItemPropertiesModel;
import org.dataintegration.service.ItemsService;
import org.dataintegration.service.MappedItemsService;
import org.dataintegration.service.ProjectsService;
import org.dataintegration.service.ScopesService;
import org.dataintegration.usecase.api.ItemsMethods;
import org.dataintegration.usecase.model.UpdateItemPropertiesRequestModel;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.LinkedList;
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

    @Transactional
    public void updateItemProperties(UUID projectId, UpdateItemPropertiesRequestModel updateItemPropertiesRequest, String key,
                                     String newValue, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        for (UUID itemId : updateItemPropertiesRequest.getItemIds()) {
            itemsService.updateItemProperty(itemId, key, newValue);
        }
    }

    public Page<ItemModel> getAllItems(UUID projectId, UUID scopeId, UUID mappingId, boolean filterMappedItems, String header,
                                       String search, String createdBy, Pageable pageable) {
        projectsService.isPermitted(projectId, createdBy);
        final LinkedList<String> extraHeaders = scopesService.getAndCheckIfScopeFinished(scopeId).getExtraHeaders();
        final Page<ItemEntity> itemEntityPage =
                itemsService.getAll(scopeId, mappingId, filterMappedItems, header, search, pageable);

        final Map<UUID, List<UUID>> itemToMappingsMap =
                mappedItemsService.getItemWithMappings(itemEntityPage.stream()
                        .map(ItemEntity::getId)
                        .toList());

        final List<ItemModel> items = itemEntityPage.stream()
                .map(itemMapper::itemEntityToItem)
                .map(item -> {
                    for (String extraHeader : extraHeaders) {
                        if (item.getProperties().get(extraHeader) == null) {
                            item.getProperties().put(extraHeader, ItemPropertiesModel.builder()
                                    .value("")
                                    .build());
                        }
                    }
                    final List<UUID> mappingIds = itemToMappingsMap.get(item.getId());
                    item.setMappingIds(mappingIds);
                    return item;
                })
                .toList();
        return new PageImpl<>(items, itemEntityPage.getPageable(), itemEntityPage.getTotalElements());
    }

}
