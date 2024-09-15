package org.datamigration.domain.service;

import lombok.RequiredArgsConstructor;
import org.datamigration.domain.model.ProjectModel;
import org.datamigration.domain.model.ScopeModel;
import org.datamigration.domain.model.ScopeTypeModel;
import org.datamigration.domain.repository.ItemRepository;
import org.datamigration.domain.repository.ProjectRepository;
import org.datamigration.domain.repository.ScopeRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataMigrationService {

    private final ProjectRepository projectRepository;
    private final ScopeRepository scopeRepository;
    private final ItemRepository itemRepository;

    public ProjectModel createOrUpdateProject(ProjectModel project) {
        project.setCreatedDate(new Date());
        project.setLastUpdatedDate(project.getCreatedDate());
        return projectRepository.save(project);
    }

    public ScopeModel addInputScope(UUID projectId, String scopeKey) {
        final ProjectModel project = findProject(projectId);

        final Set<ScopeModel> scopes = project.getScopes();

        ScopeModel scope = scopes.stream()
                .filter(s -> s.getKey().equals(scopeKey))
                .findFirst()
                .orElse(null);

        if (scope == null) {
            scope = ScopeModel.builder()
                    .key(scopeKey)
                    .createdDate(new Date())
                    .type(ScopeTypeModel.INPUT)
                    .finished(false)
                    .build();
            return scopeRepository.save(project, scope);
        }

        return scope;
    }

    public ProjectModel findProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow();
    }

    public ScopeModel findScope(UUID projectId, String scopeKey) {
        return projectRepository.findById(projectId)
                .map(project -> project.getScopes().stream()
                        .filter(s -> s.getKey().equals(scopeKey))
                        .findFirst()
                        .orElseThrow())
                .orElseThrow();
    }

    public Set<String> findScopeNames(UUID projectId) {
        return projectRepository.findById(projectId)
                .map(project -> project.getScopes().stream()
                        .map(ScopeModel::getKey)
                        .collect(Collectors.toSet()))
                .orElseThrow();
    }

    public void deleteItems(List<UUID> itemIds) {
        itemRepository.deleteAllById(itemIds);
    }

}
