package org.datamigration.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.datamigration.domain.exception.ProjectForbiddenException;
import org.datamigration.domain.model.ScopeModel;
import org.datamigration.usecase.ImportDataUsecase;
import org.datamigration.usecase.ProjectsUsecase;
import org.datamigration.usecase.ScopesUsecase;
import org.datamigration.usecase.model.CreateProjectsRequestModel;
import org.datamigration.usecase.model.ProjectInformationModel;
import org.datamigration.utils.DataMigrationUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@Tag(name = "/projects")
@RestController
@RequestMapping("${server.root.path}/projects")
@RequiredArgsConstructor
public class ProjectsRestController {

    private final ProjectsUsecase projects;
    private final ImportDataUsecase importData;
    private final ScopesUsecase getScope;

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping
    public ProjectInformationModel createNewProject(@AuthenticationPrincipal Jwt jwt,
                                                    @RequestBody CreateProjectsRequestModel createProjectsRequest) {
        return projects.createNew(createProjectsRequest, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PutMapping("/import-data")
    public void addItems(@AuthenticationPrincipal Jwt jwt, @RequestParam String bucket, @RequestParam String key)
            throws ProjectForbiddenException {
        importData.importFromS3(bucket, key, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping
    public Page<ProjectInformationModel> getProjects(@ParameterObject Pageable pageable) {
        return projects.getAll(pageable);
    }

    @GetMapping("/{projectId}/scopes/{scope}")
    public ScopeModel findScope(@PathVariable UUID projectId, @PathVariable String scope) {
        return getScope.get(projectId, scope);
    }

    @GetMapping("/{projectId}/scopes")
    public Set<String> findScopeNames(@PathVariable UUID projectId) {
        return getScope.getNames(projectId);
    }

//    @DeleteMapping("/items")
//    public void deleteItems(@RequestParam List<UUID> itemIds) {
//        items.delete(itemIds);
//    }

}
