package org.datamigration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectModel {

    private UUID id;
    private String name;
    private String owner;
    private Date createdDate;
    private Date lastUpdatedDate;

    @Builder.Default
    private Set<ScopeModel> scopes = new HashSet<>();

}
