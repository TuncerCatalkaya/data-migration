import { configureStore, Store } from "@reduxjs/toolkit"
import BusySlice from "../features/busy/busy.slice"
import AuthSlice from "../features/auth/auth.slice"
import { Dispatch, RootState } from "./store.types"
import { TypedUseSelectorHook, useDispatch, useSelector } from "react-redux"
import { AuthApi } from "../features/auth/auth.api"
import { ProjectsApi } from "../features/projects/projects.api"

export const store: Store = configureStore({
    reducer: {
        busy: BusySlice.reducer,
        auth: AuthSlice.reducer,
        [AuthApi.reducerPath]: AuthApi.reducer,
        [ProjectsApi.reducerPath]: ProjectsApi.reducer
    },
    middleware: getDefaultMiddleware => getDefaultMiddleware({ serializableCheck: false }).concat(AuthApi.middleware).concat(ProjectsApi.middleware)
})

export const useAppDispatch: () => Dispatch = useDispatch
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector
