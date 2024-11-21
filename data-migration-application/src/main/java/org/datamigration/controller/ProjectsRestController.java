package org.datamigration.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.datamigration.model.ItemModel;
import org.datamigration.model.MappedItemModel;
import org.datamigration.model.MappingModel;
import org.datamigration.model.ProjectModel;
import org.datamigration.model.ScopeModel;
import org.datamigration.usecase.ImportDataUsecase;
import org.datamigration.usecase.ProjectsUsecase;
import org.datamigration.usecase.model.ApplyMappingRequestModel;
import org.datamigration.usecase.model.ApplyUnmappingRequestModel;
import org.datamigration.usecase.model.CreateOrUpdateMappingsRequestModel;
import org.datamigration.usecase.model.CreateProjectsRequestModel;
import org.datamigration.usecase.model.CurrentCheckpointStatusResponseModel;
import org.datamigration.usecase.model.GetScopeHeadersResponseModel;
import org.datamigration.usecase.model.UpdateItemPropertiesRequestModel;
import org.datamigration.usecase.model.UpdateProjectsRequestModel;
import org.datamigration.utils.DataMigrationUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Tag(name = "/projects")
@RestController
@RequestMapping("${server.root.path}/projects")
@RequiredArgsConstructor
public class ProjectsRestController {

    private final ProjectsUsecase projectsUsecase;
    private final ImportDataUsecase importDataUsecase;

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping
    public ProjectModel createProject(@RequestBody CreateProjectsRequestModel createProjectsRequest) {
        return projectsUsecase.getProjectsMethods().createNewProject(createProjectsRequest);
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping(value = "/import-data-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importDataFile(@AuthenticationPrincipal Jwt jwt,
                               @RequestParam UUID projectId,
                               @RequestParam UUID scopeId,
                               @RequestParam String delimiter,
                               @RequestParam MultipartFile file) throws IOException {
        importDataUsecase.importFromFile(file.getBytes(), projectId, scopeId,
                DataMigrationUtils.delimiterStringToCharMapper(delimiter), DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping("/import-data-s3")
    public void importDataS3(@AuthenticationPrincipal Jwt jwt, @RequestParam UUID scopeId,
                             @RequestParam String bucket, @RequestParam String key) {
        importDataUsecase.importFromS3(scopeId, bucket, key, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping("/import-data-interrupt")
    public void interruptScope(@AuthenticationPrincipal Jwt jwt, @RequestParam UUID projectId, @RequestParam UUID scopeId) {
        projectsUsecase.getScopesMethods().interruptScope(projectId, scopeId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping("/{projectId}/scopes/{scopeId}/extra-header")
    public void addExtraHeader(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID scopeId,
                               @RequestParam String extraHeader) {
        projectsUsecase.getScopesMethods().addExtraHeader(projectId, scopeId, extraHeader, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping("/{projectId}/mappings/apply-map")
    public void applyMapping(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                             @RequestBody ApplyMappingRequestModel applyMappingRequest) {
        projectsUsecase.getMappingsMethods()
                .applyMapping(projectId, applyMappingRequest, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping("/{projectId}/mapped-items/apply-unmap")
    public void applyUnmapping(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                               @RequestBody ApplyUnmappingRequestModel applyUnmappingRequest) {
        projectsUsecase.getMappedItemsMethods()
                .deleteMappedItems(projectId, applyUnmappingRequest, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PutMapping
    public ProjectModel updateProject(@AuthenticationPrincipal Jwt jwt,
                                      @RequestBody UpdateProjectsRequestModel updateProjectsRequest) {
        return projectsUsecase.getProjectsMethods().updateProject(updateProjectsRequest, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PutMapping("/{projectId}/scopes")
    public ScopeModel createOrGetScope(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                       @RequestParam String scopeKey, @RequestParam boolean external) {
        return projectsUsecase.getScopesMethods()
                .createOrGetScope(projectId, scopeKey, external, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PutMapping("/{projectId}/items/{itemId}/properties/{key}")
    public ItemModel updateItemProperty(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                        @PathVariable UUID itemId, @PathVariable String key, @RequestParam String newValue) {
        return projectsUsecase.getItemsMethods()
                .updateItemProperty(projectId, itemId, key, newValue, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PutMapping("/{projectId}/items/bulk/properties/{key}")
    public void updateItemProperties(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                   @RequestBody UpdateItemPropertiesRequestModel updateItemPropertiesRequest,
                                   @PathVariable String key, @RequestParam String newValue) {
        projectsUsecase.getItemsMethods().updateItemProperties(projectId, updateItemPropertiesRequest, key, newValue,
                DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PutMapping("/{projectId}/mapped-items/{mappedItemId}/properties/{key}")
    public MappedItemModel updateMappedItemProperty(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                                    @PathVariable UUID mappedItemId, @PathVariable String key,
                                                    @RequestParam String newValue) {
        return projectsUsecase.getMappedItemsMethods()
                .updateMappedItemProperty(projectId, mappedItemId, key, newValue, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PutMapping("/{projectId}/scopes/{scopeId}/mappings")
    public MappingModel createOrUpdateMapping(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                              @PathVariable UUID scopeId,
                                              @RequestBody CreateOrUpdateMappingsRequestModel createMappingsRequest) {
        return projectsUsecase.getMappingsMethods()
                .createOrUpdateMapping(projectId, scopeId, createMappingsRequest, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/{projectId}/permitted")
    public void isProjectPermitted(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        projectsUsecase.getProjectsMethods().isProjectPermitted(projectId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/{projectId}")
    public ProjectModel getProject(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        return projectsUsecase.getProjectsMethods().getProject(projectId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping
    public Page<ProjectModel> getProjects(@AuthenticationPrincipal Jwt jwt, @ParameterObject Pageable pageable) {
        return projectsUsecase.getProjectsMethods().getAllProjects(DataMigrationUtils.getJwtUserId(jwt), pageable);
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/{projectId}/scopes/{scopeId}/headers")
    public GetScopeHeadersResponseModel getScopeHeaders(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                                        @PathVariable UUID scopeId) {
        return projectsUsecase.getScopesMethods().getScopeHeaders(projectId, scopeId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/{projectId}/scopes")
    public List<ScopeModel> getScopes(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        return projectsUsecase.getScopesMethods().getAllScopes(projectId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/{projectId}/scopes/{scopeId}/items")
    public Page<ItemModel> getItems(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID scopeId,
                                    @RequestParam(required = false) UUID mappingId, @RequestParam boolean filterMappedItems,
                                    @RequestParam String header, @RequestParam String search,
                                    @ParameterObject Pageable pageable) {
        return projectsUsecase.getItemsMethods().getAllItems(projectId, scopeId, mappingId, filterMappedItems, header, search,
                DataMigrationUtils.getJwtUserId(jwt), pageable);
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/{projectId}/scopes/{scopeId}/mappings")
    public List<MappingModel> getMappings(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                          @PathVariable UUID scopeId) {
        return projectsUsecase.getMappingsMethods().getAllMappings(projectId, scopeId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/{projectId}/mappings/{mappingId}/mapped-items")
    public Page<MappedItemModel> getMappedItems(@AuthenticationPrincipal Jwt jwt,
                                                @PathVariable UUID projectId, @PathVariable UUID mappingId,
                                                @ParameterObject Pageable pageable) {
        return projectsUsecase.getMappedItemsMethods()
                .getAllMappedItems(projectId, mappingId, DataMigrationUtils.getJwtUserId(jwt), pageable);
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/{projectId}/scopes/{scopeId}/checkpoints/status")
    public CurrentCheckpointStatusResponseModel getCheckpointsStatus(@AuthenticationPrincipal Jwt jwt,
                                                                     @PathVariable UUID projectId,
                                                                     @PathVariable UUID scopeId) {
        return projectsUsecase.getCheckpointsMethods()
                .getCurrentCheckpointStatus(projectId, scopeId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @DeleteMapping("/{projectId}/mark")
    public void markProjectForDeletion(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        projectsUsecase.getProjectsMethods().markProjectForDeletion(projectId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @DeleteMapping("/{projectId}/scopes/{scopeId}/mark")
    public void markScopeForDeletion(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID scopeId) {
        projectsUsecase.getScopesMethods().markScopeForDeletion(projectId, scopeId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @DeleteMapping("/{projectId}/mappings/{mappingId}/mark")
    public void markMappingForDeletion(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                       @PathVariable UUID mappingId) {
        projectsUsecase.getMappingsMethods().markMappingForDeletion(projectId, mappingId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @DeleteMapping("/{projectId}/scopes/{scopeId}/extra-header")
    public void removeExtraHeader(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID scopeId,
                                  @RequestParam String extraHeader) {
        projectsUsecase.getScopesMethods()
                .removeExtraHeader(projectId, scopeId, extraHeader, DataMigrationUtils.getJwtUserId(jwt));
    }

}
