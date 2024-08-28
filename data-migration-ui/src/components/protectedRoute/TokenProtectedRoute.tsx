import { ReactNode, useEffect } from "react"
import useAuth from "../../features/auth/hooks/useAuth"

interface TokenProtectedRouteProps {
    component: ReactNode
}

export default function TokenProtectedRoute(tokenProtectedRouteProps: Readonly<TokenProtectedRouteProps>) {
    const auth = useAuth()

    useEffect(() => {
        auth.refreshToken()
    }, [auth])

    return <>{tokenProtectedRouteProps.component}</>
}
