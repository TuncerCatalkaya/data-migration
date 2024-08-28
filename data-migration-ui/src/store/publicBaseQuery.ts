import BusySlice from "../features/busy/busy.slice"
import { BaseQueryFn, FetchArgs, fetchBaseQuery } from "@reduxjs/toolkit/query"
import { DataMigrationExtraOptions } from "./store.types"
import i18next from "i18next"

export const publicBaseQuery = (baseUrl: string): BaseQueryFn<string | FetchArgs> => {
    return async (args, api, extraOptions: DataMigrationExtraOptions) => {
        try {
            if (!extraOptions?.skipBusy) {
                api.dispatch(BusySlice.actions.setBusy(extraOptions?.busyText ? i18next.t(extraOptions.busyText) : undefined))
            }

            const baseQuery = fetchBaseQuery({
                baseUrl,
                credentials: "include"
            })

            return await baseQuery(args, api, extraOptions)
        } catch (e) {
            return { error: () => console.error("Error occurred", e) }
        } finally {
            if (!extraOptions?.skipBusy) {
                api.dispatch(BusySlice.actions.setIdle(extraOptions?.busyText ? i18next.t(extraOptions.busyText) : undefined))
            }
        }
    }
}
