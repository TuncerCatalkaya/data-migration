package org.datamigration.mapper;

import org.datamigration.jpa.entity.DatabaseEntity;
import org.datamigration.model.DatabaseModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DatabaseMapper {
    @Mapping(target = "inUse", expression = "java(!databaseEntity.getMappings().isEmpty())")
    DatabaseModel databaseEntityToDatabase(DatabaseEntity databaseEntity);
    DatabaseEntity databaseToDatabaseEntity(DatabaseModel database);
}
