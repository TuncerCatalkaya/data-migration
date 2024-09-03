import { createApi } from "@reduxjs/toolkit/query/react"
import { protectedBaseQuery } from "../../store/protectedBaseQuery"
import { CreateProjectRequest, GetProjectsRequest, GetProjectsResponse, ProjectInformationResponse } from "./projects.types"

const projectsUrl = import.meta.env.VITE_BASE_URL_ROOT_PATH + "/projects"

export const ProjectsApi = createApi({
    reducerPath: "projectsApi",
    baseQuery: protectedBaseQuery(import.meta.env.VITE_BASE_URL),
    endpoints: builder => ({
        createProject: builder.mutation<ProjectInformationResponse, CreateProjectRequest>({
            query: args => ({
                url: projectsUrl,
                method: "POST",
                body: {
                    projectName: args.projectName
                }
            }),
            extraOptions: {
                busyText: "features.projects.createProject.busyText"
            }
        }),
        getProjects: builder.query<GetProjectsResponse, GetProjectsRequest>({
            query: args => ({
                url: projectsUrl,
                method: "GET",
                params: args
            }),
            extraOptions: {
                skipBusy: true
            }
        })
    })
})
