package org.datamigration.mapper;

import org.datamigration.jpa.entity.MappedItemEntity;
import org.datamigration.model.MappedItemModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = ItemMapper.class)
public interface MappedItemMapper {
    MappedItemModel mappedItemEntityToMappedItem(MappedItemEntity mappedItemEntity);
}
