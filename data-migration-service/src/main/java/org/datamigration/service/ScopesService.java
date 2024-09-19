package org.datamigration.service;

import lombok.RequiredArgsConstructor;
import org.datamigration.exception.ScopeNotFoundException;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.jpa.repository.JpaScopeRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScopesService {

    private final JpaScopeRepository jpaScopeRepository;

    public ScopeEntity get(UUID scopeId) {
        return jpaScopeRepository.findById(scopeId)
                .orElseThrow(() -> new ScopeNotFoundException("Scope with id " + scopeId + " not found."));
    }

    public void finish(UUID scopeId) {
        jpaScopeRepository.finish(scopeId);
    }

    public void delete(UUID scopeId) {
        jpaScopeRepository.deleteById(scopeId);
    }

}
