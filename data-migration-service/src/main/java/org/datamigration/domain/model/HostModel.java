package org.datamigration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostModel {

    private UUID id;
    private String name;

    @Builder.Default
    private List<DatabaseModel> databases = new ArrayList<>();

}
