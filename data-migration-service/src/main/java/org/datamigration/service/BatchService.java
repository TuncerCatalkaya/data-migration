package org.datamigration.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.CheckpointBatchesEntity;
import org.datamigration.jpa.entity.CheckpointEntity;
import org.datamigration.jpa.repository.JpaCheckpointBatchesRepository;
import org.datamigration.jpa.repository.JpaItemRepository;
import org.datamigration.model.BatchProcessingModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final JpaItemRepository jpaItemRepository;
    private final JpaCheckpointBatchesRepository jpaCheckpointBatchesRepository;

    @Transactional
    public void processBatch(BatchProcessingModel batchProcessing, CheckpointEntity checkpointEntity) {
        jpaItemRepository.saveAll(batchProcessing.getBatch());

        final CheckpointBatchesEntity checkpointBatchesEntity = new CheckpointBatchesEntity();
        checkpointBatchesEntity.setBatchIndex(batchProcessing.getBatchIndex());
        checkpointBatchesEntity.setCheckpoint(checkpointEntity);
        jpaCheckpointBatchesRepository.save(checkpointBatchesEntity);
    }

}
