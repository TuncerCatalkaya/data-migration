package org.datamigration.service;

import lombok.RequiredArgsConstructor;
import org.datamigration.cache.DataMigrationCache;
import org.datamigration.exception.ScopeNotFoundException;
import org.datamigration.jpa.entity.ProjectEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.jpa.repository.JpaScopeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScopesService {

    private final JpaScopeRepository jpaScopeRepository;
    private final DataMigrationCache dataMigrationCache;

    public ScopeEntity createOrGetScope(ProjectEntity projectEntity, String scopeKey, boolean external) {
        return get(projectEntity.getId(), scopeKey)
                .orElseGet(() -> {
                    final ScopeEntity scopeEntity = new ScopeEntity();
                    scopeEntity.setKey(scopeKey);
                    scopeEntity.setCreatedDate(new Date());
                    scopeEntity.setExternal(external);
                    scopeEntity.setFinished(false);
                    scopeEntity.setDelete(false);
                    scopeEntity.setProject(projectEntity);
                    return jpaScopeRepository.save(scopeEntity);
                });
    }

    public ScopeEntity get(UUID scopeId) {
        final ScopeEntity scopeEntity = jpaScopeRepository.findById(scopeId)
                .orElseThrow(() -> new ScopeNotFoundException("Scope with id " + scopeId + " not found."));
        return checkIfScopeIsMarkedForDeletion(scopeEntity);
    }

    private ScopeEntity checkIfScopeIsMarkedForDeletion(ScopeEntity scopeEntity) {
        if (scopeEntity.isDelete()) {
            throw new ScopeNotFoundException("Scope with id " + scopeEntity.getId() + " is marked for deletion.");
        }
        return scopeEntity;
    }

    public Optional<ScopeEntity> get(UUID projectId, String scopeKey) {
        return jpaScopeRepository.findByProject_IdAndKey(projectId, scopeKey)
                .map(this::checkIfScopeIsMarkedForDeletion);
    }

    public List<ScopeEntity> getAll(UUID projectId) {
        return jpaScopeRepository.findAllByProject_idAndDeleteFalse(projectId, Sort.by(Sort.Direction.ASC, "createdDate"));
    }

    public void finish(UUID scopeId) {
        jpaScopeRepository.finish(scopeId);
    }

    public void updateHeaders(UUID scopeId, String[] headers) {
        jpaScopeRepository.updateHeaders(scopeId, headers);
    }

    public void markForDeletion(UUID scopeId) {
        jpaScopeRepository.markForDeletion(scopeId);
        dataMigrationCache.getMarkedForDeletionScopes().add(scopeId);
    }

}
