package org.datamigration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScopeModel {

    private UUID id;
    private Date createdDate;

    @Builder.Default
    private List<ItemModel> items = new ArrayList<>();

}
