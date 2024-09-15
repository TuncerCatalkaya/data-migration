package org.datamigration.domain.model;

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
public class ScopeModel {

    private UUID id;
    private String key;
    private Date createdDate;
    private boolean finished;
    private ScopeTypeModel type;

}
