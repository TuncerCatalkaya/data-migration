import { createApi } from "@reduxjs/toolkit/query/react"
import { protectedBaseQuery } from "../../store/protectedBaseQuery"
import {
    CreateProjectRequest,
    GetCurrentCheckpointStatusRequest,
    GetProjectRequest,
    GetProjectsRequest,
    GetProjectsResponse,
    ImportDataFileRequest,
    ImportDataResponse,
    ImportDataS3Request,
    ProjectInformationResponse,
    UpdateProjectRequest
} from "./projects.types"
import GetFrontendEnvironment from "../../utils/GetFrontendEnvironment"

const projectsUrl = "/projects"

export const ProjectsApi = createApi({
    reducerPath: "projectsApi",
    baseQuery: protectedBaseQuery(),
    endpoints: builder => ({
        createProject: builder.mutation<ProjectInformationResponse, CreateProjectRequest>({
            query: ({ projectName }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl,
                method: "POST",
                body: {
                    projectName
                }
            })
        }),
        updateProject: builder.mutation<ProjectInformationResponse, UpdateProjectRequest>({
            query: ({ projectId, projectName }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl,
                method: "PUT",
                body: {
                    projectId,
                    projectName
                }
            })
        }),
        importDataFile: builder.mutation<ImportDataResponse, ImportDataFileRequest>({
            query: args => {
                const formData = new FormData()
                formData.append("projectId", args.projectId)
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
        getProject: builder.query<ProjectInformationResponse, GetProjectRequest>({
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
        getCurrentCheckpointStatus: builder.query<GetProjectsResponse, GetCurrentCheckpointStatusRequest>({
            query: ({ projectId, scopeId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}/checkpoints/status`,
                method: "GET"
            }),
            extraOptions: {
                skipBusy: true
            }
        })
    })
})
