package org.datamigration.mapper;

import org.datamigration.jpa.entity.HostEntity;
import org.datamigration.usecase.model.CreateOrUpdateHostsRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CreateOrUpdateHostsMapper {
    HostEntity createOrUpdateHostsToHostEntity(CreateOrUpdateHostsRequestModel createOrUpdateHostsRequest);
}
