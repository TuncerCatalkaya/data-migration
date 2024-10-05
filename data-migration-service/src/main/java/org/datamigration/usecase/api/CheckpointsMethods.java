package org.datamigration.usecase.api;

import org.datamigration.usecase.model.CurrentCheckpointStatusResponseModel;

import java.util.UUID;

public interface CheckpointsMethods {
    CurrentCheckpointStatusResponseModel getCurrentCheckpointStatus(UUID projectId, UUID scopeId, String createdBy);
}
