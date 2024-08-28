package org.datamigration.usecase;

import org.datamigration.domain.model.ScopeModel;
import org.datamigration.domain.service.DataMigrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Transactional
@Component
@RequiredArgsConstructor
public class GetScopeUsecase {

    private final DataMigrationService dataMigrationService;

    public ScopeModel get(UUID projectId, String scope) {
        return dataMigrationService.findScope(projectId, scope);
    }

    public Set<String> getNames(UUID projectId) {
        return dataMigrationService.findScopeNames(projectId);
    }

}
