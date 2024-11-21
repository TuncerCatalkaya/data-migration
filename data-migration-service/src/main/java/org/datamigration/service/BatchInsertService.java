package org.datamigration.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.CheckpointBatchEntity;
import org.datamigration.jpa.entity.CheckpointEntity;
import org.datamigration.jpa.repository.JpaCheckpointBatchRepository;
import org.datamigration.jpa.repository.JpaItemRepository;
import org.datamigration.model.BatchProcessingModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchInsertService {

    private final JpaItemRepository jpaItemRepository;
    private final JpaCheckpointBatchRepository jpaCheckpointBatchRepository;

    @Transactional
    public void insertBatch(BatchProcessingModel batchProcessing, CheckpointEntity checkpointEntity) {
        jpaItemRepository.saveAll(batchProcessing.getBatch());

        final CheckpointBatchEntity checkpointBatchEntity = new CheckpointBatchEntity();
        checkpointBatchEntity.setBatchIndex(batchProcessing.getBatchIndex());
        checkpointBatchEntity.setCheckpoint(checkpointEntity);
        jpaCheckpointBatchRepository.save(checkpointBatchEntity);
    }

}
