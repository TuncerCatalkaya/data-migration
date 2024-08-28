package org.datamigration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemModel {

    private UUID id;
    private StatusModel status;

    @Builder.Default
    private Map<String, String> properties = new HashMap<>();

}
