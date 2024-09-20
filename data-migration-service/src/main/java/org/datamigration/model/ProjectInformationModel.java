package org.datamigration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInformationModel {

    private UUID id;
    private String name;
    private String owner;
    private Date createdDate;
    private Date lastUpdatedDate;

}
