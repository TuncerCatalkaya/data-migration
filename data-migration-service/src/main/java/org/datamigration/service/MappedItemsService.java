package org.datamigration.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.MappedItemEntity;
import org.datamigration.jpa.repository.JpaMappedItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

}
