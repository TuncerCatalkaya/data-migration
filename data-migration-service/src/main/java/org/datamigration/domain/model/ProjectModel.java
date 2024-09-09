package org.datamigration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
    private Map<String, ScopeModel> inputScopes = new HashMap<>();

    @Builder.Default
    private Map<String, ScopeModel> outputScopes = new HashMap<>();

}
