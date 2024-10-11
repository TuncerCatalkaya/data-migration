package org.datamigration.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.datamigration.exception.MappedItemNotFoundException;
import org.datamigration.jpa.entity.MappedItemEntity;
import org.datamigration.jpa.entity.MappingEntity;
import org.datamigration.jpa.repository.JpaMappedItemRepository;
import org.datamigration.model.ItemPropertiesModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MappedItemsService {

    private final JpaMappedItemRepository jpaMappedItemRepository;

    public void applyMapping(List<MappedItemEntity> mappedItemEntities) {
        jpaMappedItemRepository.saveAll(mappedItemEntities);
    }

    @Transactional
    public Map<UUID, List<UUID>> getItemWithMappings(List<UUID> itemIds) {
        final Map<UUID, List<UUID>> itemToMappingsMap = new HashMap<>();
        itemIds.forEach(itemId -> {
            final List<MappedItemEntity> mappedItemEntities = jpaMappedItemRepository.findAllByItem_Id(itemId);
            itemToMappingsMap.put(itemId, mappedItemEntities.stream()
                    .map(mappedItemEntity -> mappedItemEntity.getMapping().getId())
                    .toList());
        });
        return itemToMappingsMap;
    }

    public Page<MappedItemEntity> getByMapping(UUID mappingId, Pageable pageable) {
        final Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.ASC, "item.lineNumber")));
        return jpaMappedItemRepository.findAllByMapping_Id(mappingId, pageRequest);
    }

    public MappedItemEntity updateMappedItemProperty(UUID mappedItemId, String mappedKey, String newValue) {
        final MappedItemEntity mappedItemEntity = getMappedItem(mappedItemId);
        final Map<String, ItemPropertiesModel> mappedItemProperties = createOrGetProperties(mappedItemEntity, mappedKey);
        final String originalValueInDatabase = Optional.ofNullable(mappedItemProperties.get(mappedKey).getOriginalValue())
                .orElseGet(() -> mappedItemProperties.get(mappedKey).getValue());
        final boolean edited = !originalValueInDatabase.equals(newValue);
        if (edited) {
            mappedItemProperties.put(mappedKey, ItemPropertiesModel.builder()
                    .value(newValue)
                    .originalValue(originalValueInDatabase)
                    .build());
        } else {
            mappedItemProperties.remove(mappedKey);
        }

        if (mappedItemProperties.isEmpty()) {
            mappedItemEntity.setProperties(null);
        } else {
            mappedItemEntity.setProperties(mappedItemProperties);
        }
        return jpaMappedItemRepository.save(mappedItemEntity);
    }

    private Map<String, ItemPropertiesModel> createOrGetProperties(MappedItemEntity mappedItemEntity, String mappedKey) {
        final Map<String, ItemPropertiesModel> mappedItemProperties =
                Optional.ofNullable(mappedItemEntity.getProperties()).orElse(new HashMap<>());
        final MappingEntity mappingEntity = mappedItemEntity.getMapping();
        for (Map.Entry<String, String[]> mappingEntry : mappingEntity.getMapping().entrySet()) {
            for (String value : mappingEntry.getValue()) {
                if (value.equals(mappedKey)) {
                    mappedItemProperties.putIfAbsent(value,
                            mappedItemEntity.getItem().getProperties().get(mappingEntry.getKey()));
                    return mappedItemProperties;
                }
            }
        }
        return mappedItemProperties;
    }

    private MappedItemEntity getMappedItem(UUID mappedItemId) {
        return jpaMappedItemRepository.findById(mappedItemId)
                .orElseThrow(() -> new MappedItemNotFoundException("Mapped item with id " + mappedItemId + " not found."));
    }

}
