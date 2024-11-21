package org.datamigration.service;

import lombok.RequiredArgsConstructor;
import org.datamigration.exception.ItemNotFoundException;
import org.datamigration.jpa.entity.ItemEntity;
import org.datamigration.jpa.repository.JpaItemRepository;
import org.datamigration.model.ItemPropertiesModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemsService {

    private final JpaItemRepository jpaItemRepository;

    public Page<ItemEntity> getAll(UUID scopeId, UUID mappingId, boolean filterMappedItems, String header, String search,
                                   Pageable pageable) {
        final Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.ASC, "line_number")));
        if (filterMappedItems && mappingId != null) {
            if (StringUtils.hasLength(search)) {
                if (StringUtils.hasText(header)) {
                    return jpaItemRepository.findAllByScopeIdAndMappingIdNotInMappedItemsAndDynamicHeader(scopeId, mappingId,
                            header, search, pageRequest);
                } else {
                    return jpaItemRepository.findAllByScopeIdAndMappingIdNotInMappedItemsWithFreeTextSearch(scopeId, mappingId,
                            search, pageRequest);
                }
            } else {
                return jpaItemRepository.findAllByScopeIdAndMappingIdNotInMappedItems(scopeId, mappingId, pageRequest);
            }
        } else {
            if (StringUtils.hasLength(search)) {
                if (StringUtils.hasText(header)) {
                    return jpaItemRepository.findAllByScopeIdAndDynamicHeader(scopeId, header, search, pageRequest);
                } else {
                    return jpaItemRepository.findAllByScopeIdWithFreeTextSearch(scopeId, search, pageRequest);
                }
            } else {
                return jpaItemRepository.findAllByScopeId(scopeId, pageRequest);
            }
        }
    }

    public List<ItemEntity> getAll(List<UUID> itemIds) {
        return jpaItemRepository.findAllById(itemIds);
    }

    public ItemEntity updateItemProperty(UUID itemId, String key, String newValue) {
        final ItemEntity itemEntity = getItem(itemId);
        final Map<String, ItemPropertiesModel> properties = itemEntity.getProperties();
        if (properties.get(key) == null) {
            properties.put(key, ItemPropertiesModel.builder()
                    .value(newValue)
                    .build());
            return jpaItemRepository.save(itemEntity);
        }
        final String originalValueInDatabase = Optional.ofNullable(properties.get(key).getOriginalValue())
                .orElseGet(() -> properties.get(key).getValue());
        final boolean edited = !originalValueInDatabase.equals(newValue);
        properties.put(key, ItemPropertiesModel.builder()
                .value(newValue)
                .originalValue(edited ? originalValueInDatabase : null)
                .build());
        return jpaItemRepository.save(itemEntity);
    }

    private ItemEntity getItem(UUID itemId) {
        return jpaItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with id " + itemId + " not found."));
    }
}
