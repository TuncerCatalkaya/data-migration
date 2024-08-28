package org.datamigration.domain.service;

import org.datamigration.domain.model.ItemModel;
import org.datamigration.domain.model.ProjectModel;
import org.datamigration.domain.model.ScopeModel;
import org.datamigration.domain.model.StatusModel;
import org.datamigration.domain.repository.ItemRepository;
import org.datamigration.domain.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DataMigrationService {

    private final ProjectRepository projectRepository;
    private final ItemRepository itemRepository;

    public ProjectModel createOrUpdateProject(ProjectModel project) {
        project.setCreatedDate(new Date());
        project.setLastUpdatedDate(project.getCreatedDate());
        return projectRepository.save(project);
    }

    public ProjectModel importItems(UUID projectId, String hostName, String databaseName, List<Map<String, String>> items) {
        final ProjectModel project = projectRepository.findById(projectId).orElseThrow();

        final Map<String, ScopeModel> scopes = project.getInputScopes();

        final String scope = hostName + "-" + databaseName;

        scopes.putIfAbsent(scope, ScopeModel.builder()
                .createdDate(new Date())
                .build());

        scopes.get(scope).getItems().addAll(items.stream()
                .map(this::createImportItem)
                .toList());

        return projectRepository.save(project);
    }

    public ProjectModel findProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow();
    }

    public ScopeModel findScope(UUID projectId, String scope) {
        return projectRepository.findById(projectId)
                .map(project -> project.getInputScopes().get(scope))
                .orElseThrow();
    }

    public Set<String> findScopeNames(UUID projectId) {
        return projectRepository.findById(projectId)
                .map(project -> project.getInputScopes().keySet())
                .orElseThrow();
    }

    public void deleteItems(List<UUID> itemIds) {
        itemRepository.deleteAllById(itemIds);
    }

    private ItemModel createImportItem(Map<String, String> item) {
        return ItemModel.builder()
                .status(StatusModel.IMPORTED)
                .properties(item)
                .build();
    }

}
