package org.datamigration.usecase;

import org.datamigration.domain.service.DataMigrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Transactional
@Component
@RequiredArgsConstructor
public class DeleteItems {

    private final DataMigrationService dataMigrationService;

    public void delete(List<UUID> itemIds) {
        dataMigrationService.deleteItems(itemIds);
    }

}
