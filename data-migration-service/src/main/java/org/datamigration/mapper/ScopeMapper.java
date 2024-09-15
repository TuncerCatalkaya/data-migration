package org.datamigration.mapper;

import org.datamigration.domain.model.ScopeModel;
import org.datamigration.jpa.entity.ScopeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScopeMapper {

    ScopeModel scopeEntityToScope(ScopeEntity scopeEntity);

    ScopeEntity scopeToScopeEntity(ScopeModel scope);

}
