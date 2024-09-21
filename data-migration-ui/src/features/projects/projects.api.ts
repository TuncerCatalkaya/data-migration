import { createApi } from "@reduxjs/toolkit/query/react"
import { protectedBaseQuery } from "../../store/protectedBaseQuery"
import {
    CreateProjectRequest,
    DeleteScopeRequest,
    GetCurrentCheckpointStatusRequest,
    GetItemsRequest,
    GetItemsResponse,
    GetProjectRequest,
    GetProjectsRequest,
    GetProjectsResponse,
    GetScopesRequest,
    ImportDataFileRequest,
    ImportDataResponse,
    ImportDataS3Request,
    ProjectResponse,
    ScopeResponse,
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
        importDataFile: builder.mutation<ImportDataResponse, ImportDataFileRequest>({
            query: args => {
                const formData = new FormData()
                formData.append("projectId", args.projectId)
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
        importDataS3: builder.mutation<ImportDataResponse, ImportDataS3Request>({
            query: args => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + "/import-data-s3",
                method: "POST",
                params: args
            }),
            extraOptions: {
                skipBusy: true
            }
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
        getScopes: builder.query<ScopeResponse[], GetScopesRequest>({
            query: ({ projectId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes`,
                method: "GET"
            })
        }),
        getItems: builder.query<GetItemsResponse, GetItemsRequest>({
            query: ({ projectId, scopeId, page, size, sort }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}/items`,
                method: "GET",
                params: {
                    page,
                    size,
                    sort
                }
            })
        }),
        getCurrentCheckpointStatus: builder.query<GetProjectsResponse, GetCurrentCheckpointStatusRequest>({
            query: ({ projectId, scopeId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}/checkpoints/status`,
                method: "GET"
            }),
            extraOptions: {
                skipBusy: true
            }
        }),
        deleteScope: builder.mutation<void, DeleteScopeRequest>({
            query: ({ projectId, scopeId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}`,
                method: "DELETE"
            })
        })
    })
})
