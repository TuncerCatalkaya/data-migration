package org.datamigration.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.datamigration.domain.exception.ProjectForbiddenException;
import org.datamigration.domain.model.ScopeModel;
import org.datamigration.usecase.CheckpointsUsecase;
import org.datamigration.usecase.ImportDataUsecase;
import org.datamigration.usecase.ProjectsUsecase;
import org.datamigration.usecase.ScopesUsecase;
import org.datamigration.usecase.model.CreateProjectsRequestModel;
import org.datamigration.usecase.model.CurrentCheckpointStatusModel;
import org.datamigration.usecase.model.ImportDataResponseModel;
import org.datamigration.usecase.model.ProjectInformationModel;
import org.datamigration.utils.DataMigrationUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@Tag(name = "/projects")
@RestController
@RequestMapping("${server.root.path}/projects")
@RequiredArgsConstructor
public class ProjectsRestController {

    private final ProjectsUsecase projectsUsecase;
    private final CheckpointsUsecase checkpointsUsecase;
    private final ImportDataUsecase importDataUsecase;
    private final ScopesUsecase scopesUsecase;

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping
    public ProjectInformationModel createNewProject(@AuthenticationPrincipal Jwt jwt,
                                                    @RequestBody CreateProjectsRequestModel createProjectsRequest) {
        return projectsUsecase.createNew(createProjectsRequest, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping(value = "/import-data-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportDataResponseModel importData(@AuthenticationPrincipal Jwt jwt, @RequestParam UUID projectId,
                                              @RequestParam MultipartFile file)
            throws ProjectForbiddenException {
        return importDataUsecase.importFromFile(file, projectId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping("/import-data-s3")
    public ImportDataResponseModel importDataS3(@AuthenticationPrincipal Jwt jwt, @RequestParam String bucket,
                                                @RequestParam String key)
            throws ProjectForbiddenException {
        return importDataUsecase.importFromS3(bucket, key, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping
    public Page<ProjectInformationModel> getProjects(@ParameterObject Pageable pageable) {
        return projectsUsecase.getAll(pageable);
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/{projectId}/scopes/{scopeId}/checkpoints/status")
    public CurrentCheckpointStatusModel getCheckpointsStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                                             @PathVariable UUID scopeId) throws ProjectForbiddenException {
        return checkpointsUsecase.getCurrentCheckpointStatus(projectId, scopeId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @GetMapping("/{projectId}/scopes/{scope}")
    public ScopeModel findScope(@PathVariable UUID projectId, @PathVariable String scope) {
        return scopesUsecase.get(projectId, scope);
    }

    @GetMapping("/{projectId}/scopes")
    public Set<String> findScopeNames(@PathVariable UUID projectId) {
        return scopesUsecase.getNames(projectId);
    }

//    @DeleteMapping("/items")
//    public void deleteItems(@RequestParam List<UUID> itemIds) {
//        items.delete(itemIds);
//    }

}
