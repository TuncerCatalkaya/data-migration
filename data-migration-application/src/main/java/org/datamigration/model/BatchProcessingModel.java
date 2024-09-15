package org.datamigration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.datamigration.jpa.entity.ItemEntity;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchProcessingModel {

    private UUID projectId;
    private UUID scopeId;
    private String fileName;
    private long batchIndex;
    private int batchSize;

    private List<ItemEntity> batch;

}
