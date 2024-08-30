import { createApi } from "@reduxjs/toolkit/query/react"
import { publicBaseQuery } from "../../store/publicBaseQuery"

export const AuthApi = createApi({
    reducerPath: "authApi",
    baseQuery: publicBaseQuery(import.meta.env.VITE_REFRESH_TOKEN_ENDPOINT_BASE_URL),
    endpoints: builder => ({
        refreshToken: builder.mutation<Record<string, string>, void>({
            query: () => ({
                url: import.meta.env.VITE_REFRESH_TOKEN_ENDPOINT_URL,
                method: import.meta.env.VITE_REFRESH_TOKEN_ENDPOINT_METHOD
            }),
            extraOptions: {
                skipBusy: true
            }
        })
    })
})
