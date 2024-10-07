package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.MappedItemEntity;
import org.datamigration.mapper.MappedItemMapper;
import org.datamigration.model.MappedItemModel;
import org.datamigration.service.MappedItemsService;
import org.datamigration.service.ProjectsService;
import org.datamigration.usecase.api.MappedItemsMethods;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
class MappedItems implements MappedItemsMethods {

    private final MappedItemMapper mappedItemMapper = Mappers.getMapper(MappedItemMapper.class);
    private final ProjectsService projectsService;
    private final MappedItemsService mappedItemsService;

    public Page<MappedItemModel> getAllMappedItemsByMapping(UUID projectId, UUID mappingId, String createdBy,
                                                            Pageable pageable) {
        projectsService.isPermitted(projectId, createdBy);
        final Page<MappedItemEntity> mappedItemEntities = mappedItemsService.getByMapping(mappingId, pageable);
        final List<MappedItemModel> mappedItems = mappedItemEntities.stream()
                .map(mappedItemMapper::mappedItemEntityToMappedItem)
                .toList();
        return new PageImpl<>(mappedItems, mappedItemEntities.getPageable(),
                mappedItemEntities.getTotalElements());
    }

}
