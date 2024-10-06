package org.datamigration.mapper;

import org.datamigration.jpa.entity.MappingEntity;
import org.datamigration.model.MappingModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = HostMapper.class)
public interface MappingMapper {
    MappingModel mappingEntityToMapping(MappingEntity mappingEntity);

}
