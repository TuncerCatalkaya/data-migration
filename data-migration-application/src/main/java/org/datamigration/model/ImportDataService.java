package org.datamigration.model;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datamigration.jpa.entity.CheckpointBatchesEntity;
import org.datamigration.jpa.entity.CheckpointEntity;
import org.datamigration.jpa.repository.JpaCheckpointBatchesRepository;
import org.datamigration.jpa.repository.JpaCheckpointRepository;
import org.datamigration.jpa.repository.JpaItemRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportDataService {

    private final JpaItemRepository jpaItemRepository;
    private final JpaCheckpointRepository jpaCheckpointRepository;
    private final JpaCheckpointBatchesRepository jpaCheckpointBatchesRepository;

    @Transactional
    public void processBatchAsync(BatchProcessingModel batchProcessing) {
        jpaItemRepository.saveAll(batchProcessing.getBatch());

        final CheckpointEntity checkpointEntity =
                jpaCheckpointRepository.findById(batchProcessing.getScopeId()).orElseThrow();

        final CheckpointBatchesEntity checkpointBatchesEntity = new CheckpointBatchesEntity();
        checkpointBatchesEntity.setBatchIndex(batchProcessing.getBatchIndex());
        checkpointBatchesEntity.setCheckpoint(checkpointEntity);
        jpaCheckpointBatchesRepository.save(checkpointBatchesEntity);

        log.info(batchProcessing.getFileName() + "(" + batchProcessing.getScopeId() + ")" + ", Processed batch " +
                batchProcessing.getBatchIndex());
    }
}
