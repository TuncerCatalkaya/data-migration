import { createApi } from "@reduxjs/toolkit/query/react"
import { protectedBaseQuery } from "../../store/protectedBaseQuery"
import {
    ApplyMappingRequest,
    CreateOrGetScopeRequest,
    CreateOrUpdateMappingsRequest,
    CreateProjectRequest,
    GetCurrentCheckpointStatusRequest,
    GetCurrentCheckpointStatusResponse,
    GetItemsRequest,
    GetItemsResponse,
    GetMappedItemsByMappingRequest,
    GetMappedItemsByMappingResponse,
    GetMappingsRequest,
    GetProjectRequest,
    GetProjectsRequest,
    GetProjectsResponse,
    GetScopeRequest,
    GetScopesRequest,
    ImportDataFileRequest,
    ImportDataS3Request,
    InterruptScopeRequest,
    IsProjectPermittedRequest,
    ItemResponse,
    MappingResponse,
    MarkMappingForDeletionRequest,
    MarkScopeForDeletionRequest,
    ProjectResponse,
    ScopeResponse,
    UpdateItemPropertyRequest,
    UpdateProjectRequest
} from "./projects.types"
import GetFrontendEnvironment from "../../utils/GetFrontendEnvironment"

const projectsUrl = "/projects"

export const ProjectsApi = createApi({
    reducerPath: "projectsApi",
    baseQuery: protectedBaseQuery(),
    endpoints: builder => ({
        createProject: builder.mutation<ProjectResponse, CreateProjectRequest>({
            query: ({ projectName }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl,
                method: "POST",
                body: {
                    projectName
                }
            })
        }),
        importDataFile: builder.mutation<void, ImportDataFileRequest>({
            query: args => {
                const formData = new FormData()
                formData.append("projectId", args.projectId)
                formData.append("scopeId", args.scopeId)
                formData.append("delimiter", args.delimiter)
                formData.append("file", args.file)
                return {
                    url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + "/import-data-file",
                    method: "POST",
                    body: formData
                }
            },
            extraOptions: {
                skipBusy: true
            }
        }),
        importDataS3: builder.mutation<void, ImportDataS3Request>({
            query: args => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + "/import-data-s3",
                method: "POST",
                params: args
            }),
            extraOptions: {
                skipBusy: true
            }
        }),
        interruptScope: builder.mutation<void, InterruptScopeRequest>({
            query: args => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + "/import-data-interrupt",
                method: "POST",
                params: args
            }),
            extraOptions: {
                skipBusy: true
            }
        }),
        applyMapping: builder.mutation<void, ApplyMappingRequest>({
            query: ({ projectId, mappingId, itemIds }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/mappings/apply`,
                method: "POST",
                body: {
                    mappingId,
                    itemIds
                }
            })
        }),
        updateProject: builder.mutation<ProjectResponse, UpdateProjectRequest>({
            query: ({ projectId, projectName }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl,
                method: "PUT",
                body: {
                    projectId,
                    projectName
                }
            })
        }),
        createOrGetScope: builder.mutation<ScopeResponse, CreateOrGetScopeRequest>({
            query: ({ projectId, scopeKey, external }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes`,
                method: "PUT",
                params: {
                    scopeKey,
                    external
                }
            })
        }),
        updateItemProperty: builder.mutation<ItemResponse, UpdateItemPropertyRequest>({
            query: ({ projectId, itemId, key, newValue }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/items/${itemId}/properties/${key}`,
                method: "PUT",
                params: {
                    newValue
                }
            })
        }),
        createOrUpdateMapping: builder.mutation<MappingResponse, CreateOrUpdateMappingsRequest>({
            query: ({ projectId, scopeId, mappingId, databaseId, mappingName, mapping }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}/mappings`,
                method: "PUT",
                body: {
                    mappingId,
                    databaseId,
                    mappingName,
                    mapping
                }
            })
        }),
        isProjectPermitted: builder.query<void, IsProjectPermittedRequest>({
            query: ({ projectId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/permitted`,
                method: "GET"
            })
        }),
        getProject: builder.query<ProjectResponse, GetProjectRequest>({
            query: ({ projectId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}`,
                method: "GET"
            })
        }),
        getProjects: builder.query<GetProjectsResponse, GetProjectsRequest>({
            query: args => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl,
                method: "GET",
                params: args
            }),
            extraOptions: {
                skipBusy: true
            }
        }),
        getScopeHeaders: builder.query<string[], GetScopeRequest>({
            query: ({ projectId, scopeId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}/headers`,
                method: "GET"
            })
        }),
        getScopes: builder.query<ScopeResponse[], GetScopesRequest>({
            query: ({ projectId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes`,
                method: "GET"
            })
        }),
        getItems: builder.query<GetItemsResponse, GetItemsRequest>({
            query: ({ projectId, scopeId, mappingId, filterMappedItems, page, size, sort }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}/items`,
                method: "GET",
                params: {
                    mappingId,
                    filterMappedItems,
                    page,
                    size,
                    sort
                }
            })
        }),
        getMappings: builder.query<MappingResponse[], GetMappingsRequest>({
            query: ({ projectId, scopeId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}/mappings`,
                method: "GET"
            })
        }),
        getMappedItemsByMapping: builder.query<GetMappedItemsByMappingResponse, GetMappedItemsByMappingRequest>({
            query: ({ projectId, mappingId, page, size, sort }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/mappings/${mappingId}/mapped-items`,
                method: "GET",
                params: {
                    page,
                    size,
                    sort
                }
            })
        }),
        getCurrentCheckpointStatus: builder.query<GetCurrentCheckpointStatusResponse, GetCurrentCheckpointStatusRequest>({
            query: ({ projectId, scopeId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}/checkpoints/status`,
                method: "GET"
            }),
            extraOptions: {
                skipBusy: true
            }
        }),
        markScopeForDeletion: builder.mutation<void, MarkScopeForDeletionRequest>({
            query: ({ projectId, scopeId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}/mark`,
                method: "DELETE"
            })
        }),
        markMappingForDeletion: builder.mutation<void, MarkMappingForDeletionRequest>({
            query: ({ projectId, mappingId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/mappings/${mappingId}/mark`,
                method: "DELETE"
            })
        })
    })
})
