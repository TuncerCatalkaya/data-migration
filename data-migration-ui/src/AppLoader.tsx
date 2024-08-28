import AuthSlice from "./features/auth/auth.slice"
import { useAppDispatch } from "./store/store"
import i18next from "i18next"
import { useEffect } from "react"
import Router from "./router/Router"

interface AppLoaderProps {
    token?: string
    language: string
}

export default function AppLoader(appLoaderProps: Readonly<AppLoaderProps>) {
    const dispatch = useAppDispatch()

    useEffect(() => {
        dispatch(AuthSlice.actions.setToken(appLoaderProps.token))
    }, [dispatch, appLoaderProps.token])

    useEffect(() => {
        i18next.changeLanguage(appLoaderProps.language)
    }, [appLoaderProps.language])

    return <Router />
}
