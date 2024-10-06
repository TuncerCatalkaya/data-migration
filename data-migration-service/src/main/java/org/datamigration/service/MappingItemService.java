package org.datamigration.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.MappingItemStatusEntity;
import org.datamigration.jpa.repository.JpaMappingItemStatusRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MappingItemService {

    private final JpaMappingItemStatusRepository jpaMappingItemStatusRepository;

    public void applyMapping(List<MappingItemStatusEntity> mappingItemStatusEntities) {
        jpaMappingItemStatusRepository.saveAll(mappingItemStatusEntities);
    }

    @Transactional
    public Map<UUID, List<UUID>> getItemMappings(List<UUID> itemIds) {
        final Map<UUID, List<UUID>> itemToMappingsMap = new HashMap<>();
        itemIds.forEach(itemId -> {
            final List<MappingItemStatusEntity> mappingItemStatusEntities =
                    jpaMappingItemStatusRepository.findAllByItem_Id(itemId);
            itemToMappingsMap.put(itemId, mappingItemStatusEntities.stream()
                    .map(mappingItemStatusEntity -> mappingItemStatusEntity.getMapping().getId())
                    .toList());
        });
        return itemToMappingsMap;
    }

}
