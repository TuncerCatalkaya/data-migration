import { createApi } from "@reduxjs/toolkit/query/react"
import { protectedBaseQuery } from "../../store/protectedBaseQuery"
import { CreateProjectRequest, GetProjectsRequest, GetProjectsResponse, ProjectInformationResponse } from "./project.types"

export const ProjectApi = createApi({
    reducerPath: "projectApi",
    baseQuery: protectedBaseQuery(import.meta.env.VITE_BASE_URL),
    endpoints: builder => ({
        createProject: builder.mutation<ProjectInformationResponse, CreateProjectRequest>({
            query: args => ({
                url: "data-migration/project/create/" + args.projectName,
                method: "POST"
            }),
            extraOptions: {
                busyText: "Creating Project..."
            }
        }),
        getProjects: builder.query<GetProjectsResponse, GetProjectsRequest>({
            query: args => ({
                url: "data-migration/project",
                method: "GET",
                params: args
            }),
            extraOptions: {
                skipBusy: true
            }
        })
    })
})
