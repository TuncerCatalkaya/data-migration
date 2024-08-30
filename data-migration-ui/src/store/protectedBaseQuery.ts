import BusySlice from "../features/busy/busy.slice"
import { BaseQueryFn, FetchArgs, fetchBaseQuery } from "@reduxjs/toolkit/query"
import { DataMigrationExtraOptions, RootState } from "./store.types"
import i18next from "i18next"
import AuthSlice from "../features/auth/auth.slice"

export const protectedBaseQuery = (baseUrl: string): BaseQueryFn<string | FetchArgs> => {
    return async (args, api, extraOptions: DataMigrationExtraOptions) => {
        try {
            if (!extraOptions?.skipBusy) {
                api.dispatch(BusySlice.actions.setBusy(extraOptions?.busyText ? i18next.t(extraOptions.busyText) : undefined))
            }

            let token: string | undefined = (api.getState() as RootState).auth.token

            const baseQuery = fetchBaseQuery({
                baseUrl,
                prepareHeaders: headers => {
                    if (token) {
                        headers.set("Authorization", `Bearer ${token}`)
                    }
                    return headers
                }
            })

            const response = await baseQuery(args, api, extraOptions)

            console.log(response)

            if (response.error && response.error.status === 401) {
                const refreshTokenResponse = await fetch(
                    import.meta.env.VITE_REFRESH_TOKEN_ENDPOINT_BASE_URL + "/" + import.meta.env.VITE_REFRESH_TOKEN_ENDPOINT_URL,
                    {
                        method: import.meta.env.VITE_REFRESH_TOKEN_ENDPOINT_METHOD,
                        credentials: "include"
                    }
                )

                token = undefined

                if (refreshTokenResponse.ok) {
                    const data = await refreshTokenResponse.json()
                    token = data[import.meta.env.VITE_REFRESH_TOKEN_ENDPOINT_ACCESS_TOKEN_NAME]
                }

                api.dispatch(AuthSlice.actions.setToken(token))

                const retryBaseQuery = fetchBaseQuery({
                    baseUrl,
                    prepareHeaders: headers => {
                        if (token) {
                            headers.set("Authorization", `Bearer ${token}`)
                        }
                        return headers
                    }
                })

                return await retryBaseQuery(args, api, extraOptions)
            }

            return response
        } catch (e) {
            return { error: () => console.error("Error occurred", e) }
        } finally {
            if (!extraOptions?.skipBusy) {
                api.dispatch(BusySlice.actions.setIdle(extraOptions?.busyText ? i18next.t(extraOptions.busyText) : undefined))
            }
        }
    }
}
