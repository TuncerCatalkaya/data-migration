package org.datamigration.usecase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.datamigration.domain.exception.ScopeNotFoundException;
import org.datamigration.domain.model.ScopeModel;
import org.datamigration.domain.service.DataMigrationService;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.jpa.repository.JpaScopeRepository;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Transactional
@Component
@RequiredArgsConstructor
public class ScopesUsecase {

    private final DataMigrationService dataMigrationService;
    private final JpaScopeRepository jpaScopeRepository;

    public ScopeModel get(UUID projectId, String scope) {
        return dataMigrationService.findScope(projectId, scope);
    }

    public Set<String> getNames(UUID projectId) {
        return dataMigrationService.findScopeNames(projectId);
    }

    public ScopeEntity get(UUID scopeId) {
        return jpaScopeRepository.findById(scopeId)
                .orElseThrow(() -> new ScopeNotFoundException("Scope with id " + scopeId + " not found."));
    }

}
