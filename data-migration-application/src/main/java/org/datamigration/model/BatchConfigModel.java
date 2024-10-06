package org.datamigration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchConfigModel {

    private int batchSize;
    private int batchRetryScopeMax;
    private int batchRetryBatchMax;

}
