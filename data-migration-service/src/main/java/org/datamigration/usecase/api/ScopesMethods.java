package org.datamigration.usecase.api;

import org.datamigration.model.ScopeModel;
import org.datamigration.usecase.model.GetScopeHeadersResponseModel;

import java.util.List;
import java.util.UUID;

public interface ScopesMethods {
    ScopeModel createOrGetScope(UUID projectId, String scopeKey, boolean external, String createdBy);
    void interruptScope(UUID projectId, UUID scopeId, String createdBy);
    GetScopeHeadersResponseModel getScopeHeaders(UUID projectId, UUID scopeId, String createdBy);
    List<ScopeModel> getAllScopes(UUID projectId, String createdBy);
    void markScopeForDeletion(UUID projectId, UUID scopeId, String createdBy);
    void addExtraHeader(UUID projectId, UUID scopeId, String extraHeader, String createdBy);
    void removeExtraHeader(UUID projectId, UUID scopeId, String extraHeader, String createdBy);
}
