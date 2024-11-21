package org.datamigration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappingModel {

    private UUID id;
    private String name;
    private Date createdDate;
    private boolean finished;
    private boolean processing;
    private Map<String, String[]> mapping;
    private SelectedDatabaseModel database;

}
