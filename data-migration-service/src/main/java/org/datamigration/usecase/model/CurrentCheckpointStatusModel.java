package org.datamigration.usecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentCheckpointStatusModel {

    private long batchesProcessed;
    private long totalBatches;
    private boolean processing;
    private boolean finished;

}
