import { createApi } from "@reduxjs/toolkit/query/react"
import { protectedBaseQuery } from "../../store/protectedBaseQuery"
import { CreateProjectRequest, CreateProjectResponse } from "./project.types"

export const ProjectApi = createApi({
    reducerPath: "projectApi",
    baseQuery: protectedBaseQuery(import.meta.env.VITE_BASE_URL),
    endpoints: builder => ({
        createProject: builder.mutation<CreateProjectResponse, CreateProjectRequest>({
            query: args => ({
                url: "/data-migration/project/create/" + args.projectName,
                method: "POST"
            }),
            extraOptions: {
                busyText: "Creating Project..."
            }
        })
    })
})
