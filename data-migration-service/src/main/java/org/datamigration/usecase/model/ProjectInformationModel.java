package org.datamigration.usecase.model;

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
    private Integer owner;
    private Date createdDate;
    private Date lastUpdatedDate;

}
