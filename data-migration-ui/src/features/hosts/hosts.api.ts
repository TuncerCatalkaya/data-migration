import { createApi } from "@reduxjs/toolkit/query/react"
import { protectedBaseQuery } from "../../store/protectedBaseQuery"
import GetFrontendEnvironment from "../../utils/GetFrontendEnvironment"
import { Host } from "./hosts.types"

const hostsUrl = "/hosts"

export const HostsApi = createApi({
    reducerPath: "hostsApi",
    baseQuery: protectedBaseQuery(),
    endpoints: builder => ({
        createOrUpdateHost: builder.mutation<Host, Host>({
            query: args => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + hostsUrl,
                method: "PUT",
                body: args
            })
        }),
        getHosts: builder.query<Host[], void>({
            query: () => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + hostsUrl,
                method: "GET"
            })
        })
    })
})
