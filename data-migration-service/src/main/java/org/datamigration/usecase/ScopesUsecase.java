package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.service.ScopesService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ScopesUsecase {

    private final ScopesService scopesService;

    public ScopeEntity get(UUID scopeId) {
        return scopesService.get(scopeId);
    }

    public void finish(UUID scopeId) {
        scopesService.finish(scopeId);
    }

    public void delete(UUID scopeId) {
        scopesService.delete(scopeId);
    }

}
