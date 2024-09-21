package org.datamigration.mapper;

import org.datamigration.jpa.entity.ItemEntity;
import org.datamigration.model.ItemModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ItemMapper {
    ItemModel itemEntityToItem(ItemEntity itemEntity);

}
