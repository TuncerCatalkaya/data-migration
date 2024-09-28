package org.datamigration.service;

import lombok.RequiredArgsConstructor;
import org.datamigration.exception.ScopeNotFoundException;
import org.datamigration.jpa.entity.ProjectEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.jpa.repository.JpaScopeRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScopesService {

    private final JpaScopeRepository jpaScopeRepository;

    public ScopeEntity createOrGetScope(ProjectEntity projectEntity, String scopeKey, boolean external) {
        return jpaScopeRepository.findByProject_IdAndKey(projectEntity.getId(), scopeKey)
                .orElseGet(() -> {
                    final ScopeEntity scopeEntity = new ScopeEntity();
                    scopeEntity.setKey(scopeKey);
                    scopeEntity.setCreatedDate(new Date());
                    scopeEntity.setExternal(external);
                    scopeEntity.setFinished(false);
                    scopeEntity.setProject(projectEntity);
                    return jpaScopeRepository.save(scopeEntity);
                });
    }

    public ScopeEntity get(UUID scopeId) {
        return jpaScopeRepository.findById(scopeId)
                .orElseThrow(() -> new ScopeNotFoundException("Scope with id " + scopeId + " not found."));
    }

    public Optional<ScopeEntity> get(UUID projectId, String scopeKey) {
        return jpaScopeRepository.findByProject_IdAndKey(projectId, scopeKey);
    }

    public Set<ScopeEntity> getAll(UUID projectId) {
        return jpaScopeRepository.findAllByProject_id(projectId);
    }

    public void finish(UUID scopeId) {
        jpaScopeRepository.finish(scopeId);
    }

    public void updateHeaders(UUID scopeId, String[] headers) {
        jpaScopeRepository.updateHeaders(scopeId, headers);
    }

    public void delete(UUID scopeId) {
        jpaScopeRepository.deleteById(scopeId);
    }

}
