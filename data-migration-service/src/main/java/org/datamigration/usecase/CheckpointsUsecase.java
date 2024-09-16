package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.CheckpointEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.jpa.repository.JpaCheckpointRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CheckpointsUsecase {

    private final JpaCheckpointRepository jpaCheckpointRepository;

    public int createOrGetCheckpointBy(ScopeEntity scopeEntity, int batchSizeGet) {
        final Optional<CheckpointEntity> foundCheckpointEntity = jpaCheckpointRepository.findByScope_Id(scopeEntity.getId());
        final int batchSize;
        if (foundCheckpointEntity.isPresent()) {
            batchSize = foundCheckpointEntity.get().getBatchSize();
        } else {
            batchSize = batchSizeGet;
            final CheckpointEntity checkpointEntity = new CheckpointEntity();
            checkpointEntity.setScope(scopeEntity);
            checkpointEntity.setBatchSize(batchSize);
            jpaCheckpointRepository.save(checkpointEntity);
        }
        return batchSize;
    }

}
