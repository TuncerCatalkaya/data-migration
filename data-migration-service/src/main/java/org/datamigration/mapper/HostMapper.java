package org.datamigration.mapper;

import org.datamigration.jpa.entity.HostEntity;
import org.datamigration.model.HostModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HostMapper {
    HostModel hostEntityToHost(HostEntity hostEntity);
    HostEntity hostToHostEntity(HostModel host);
}
