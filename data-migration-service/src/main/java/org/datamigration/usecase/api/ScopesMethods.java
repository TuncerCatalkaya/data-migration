package org.datamigration.usecase.api;

import org.datamigration.model.ScopeModel;

import java.util.List;
import java.util.UUID;

public interface ScopesMethods {
    ScopeModel createOrGetScope(UUID projectId, String scopeKey, boolean external, String owner);
    void interruptScope(UUID projectId, UUID scopeId, String owner);
    String[] getScopeHeaders(UUID projectId, UUID scopeId, String owner);
    List<ScopeModel> getAllScopes(UUID projectId, String owner);
    void deleteScope(UUID projectId, UUID scopeId, String owner);
}
