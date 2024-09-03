package org.datamigration.usecase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.datamigration.domain.model.ProjectModel;
import org.datamigration.domain.service.DataMigrationService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Transactional
@Component
@RequiredArgsConstructor
public class ItemsUsecase {

    private final DataMigrationService dataMigrationService;

    public ProjectModel importData(UUID projectId, String hostName, String databaseName, List<Map<String, String>> items) {
        return dataMigrationService.importItems(projectId, hostName, databaseName, items);
    }

    public void delete(List<UUID> itemIds) {
        dataMigrationService.deleteItems(itemIds);
    }

}
