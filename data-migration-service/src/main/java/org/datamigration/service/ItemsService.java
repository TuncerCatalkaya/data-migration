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

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemsService {

    private final JpaItemRepository jpaItemRepository;

    public Page<ItemEntity> getAll(UUID scopeId, Pageable pageable) {
        final Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.ASC, "lineNumber")));
        return jpaItemRepository.findAllByScope_Id(scopeId, pageRequest);
    }

    public ItemEntity updateItemProperty(UUID itemId, String key, String newValue) {
        final ItemEntity itemEntity = getItem(itemId);
        final String originalValueInDatabase = Optional.ofNullable(itemEntity.getProperties().get(key).getOriginalValue())
                .orElseGet(() -> itemEntity.getProperties().get(key).getValue());
        final boolean edited = !originalValueInDatabase.equals(newValue);
        itemEntity.getProperties().put(key, ItemPropertiesModel.builder()
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
