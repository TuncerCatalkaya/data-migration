package org.datamigration.controller;

import lombok.RequiredArgsConstructor;
import org.datamigration.domain.model.HostModel;
import org.datamigration.domain.model.ProjectModel;
import org.datamigration.domain.model.ScopeModel;
import org.datamigration.usecase.CreateHostUsecase;
import org.datamigration.usecase.DeleteHostUsecase;
import org.datamigration.usecase.DeleteItems;
import org.datamigration.usecase.GetScopeUsecase;
import org.datamigration.usecase.ImportItemsUsecase;
import org.datamigration.usecase.ProjectUsecase;
import org.datamigration.usecase.model.ProjectInformationModel;
import org.datamigration.utils.DataMigrationUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/data-migration")
@RequiredArgsConstructor
public class DataMigrationRestController {

    private final ProjectUsecase project;
    private final ImportItemsUsecase importItems;
    private final DeleteItems deleteItems;
    private final GetScopeUsecase getScope;
    private final CreateHostUsecase createHost;
    private final DeleteHostUsecase deleteHost;

    @PreAuthorize("hasAnyAuthority('fish-ROLE_SUPER_USER')")
    @PostMapping("/project/create/{projectName}")
    public ProjectInformationModel createNewProject(@AuthenticationPrincipal Jwt jwt, @PathVariable String projectName) {
        return project.createNew(projectName, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PutMapping("/items/import/{projectId}")
    public ProjectModel addItems(@PathVariable UUID projectId, @RequestParam String hostName, @RequestParam String databaseName,
                                 @RequestBody List<Map<String, String>> items) {
        return importItems.importData(projectId, hostName, databaseName, items);
    }

    @PutMapping("/host/create-or-update")
    public HostModel addItems(@RequestBody HostModel host) {
        return createHost.createOrUpdate(host);
    }

    @GetMapping("/scope/{projectId}/{scope}/")
    public ScopeModel findScope(@PathVariable UUID projectId, @PathVariable String scope) {
        return getScope.get(projectId, scope);
    }

    @GetMapping("/scope/{projectId}")
    public Set<String> findScopeNames(@PathVariable UUID projectId) {
        return getScope.getNames(projectId);
    }

    @DeleteMapping("/host/delete/{hostId}")
    public void deleteHost(@PathVariable UUID hostId) {
        deleteHost.delete(hostId);
    }

    @DeleteMapping("/items/delete")
    public void deleteItems(@RequestParam List<UUID> itemIds) {
        deleteItems.delete(itemIds);
    }
}
