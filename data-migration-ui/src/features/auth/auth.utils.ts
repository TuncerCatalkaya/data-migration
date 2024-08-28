import { jwtDecode, JwtPayload } from "jwt-decode"

export const AuthUtils = {
    isJwtTokenValid(token?: string) {
        return token ? !isTokenExpired(token) : false
    }
}

function isTokenExpired(token: string) {
    try {
        const decodedToken = jwtDecode<JwtPayload>(token)

        if (decodedToken.exp) {
            const currentTime = Math.floor(Date.now() / 1000)
            return decodedToken.exp < currentTime
        }

        return false
    } catch (error) {
        return true
    }
}
