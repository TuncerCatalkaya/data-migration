import { configureStore, Store } from "@reduxjs/toolkit"
import BusySlice from "../features/busy/busy.slice"
import AuthSlice from "../features/auth/auth.slice"
import { Dispatch, RootState } from "./store.types"
import { TypedUseSelectorHook, useDispatch, useSelector } from "react-redux"
import { AuthApi } from "../features/auth/auth.api"

export const store: Store = configureStore({
    reducer: {
        busy: BusySlice.reducer,
        auth: AuthSlice.reducer,
        [AuthApi.reducerPath]: AuthApi.reducer
    },
    middleware: getDefaultMiddleware => getDefaultMiddleware({ serializableCheck: false }).concat(AuthApi.middleware)
})

export const useAppDispatch: () => Dispatch = useDispatch
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector
