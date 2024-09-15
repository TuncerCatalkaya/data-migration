package org.datamigration.usecase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.datamigration.domain.service.DataMigrationService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Transactional
@Component
@RequiredArgsConstructor
public class ItemsUsecase {

    private final DataMigrationService dataMigrationService;

    public void delete(List<UUID> itemIds) {
        dataMigrationService.deleteItems(itemIds);
    }

}
