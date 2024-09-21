package org.datamigration.mapper;

import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.model.ScopeModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScopeMapper {
    ScopeModel scopeEntityToScope(ScopeEntity scopeEntity);

}
