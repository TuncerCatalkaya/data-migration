import { Button, Stack } from "@mui/material"
import { useTranslation } from "react-i18next"
import { AuthApi } from "../../features/auth/auth.api"
import { useAppDispatch } from "../../store/store"
import AuthSlice from "../../features/auth/auth.slice"

export default function ProjectsPage() {
    const translation = useTranslation()
    const dispatch = useAppDispatch()
    const [refreshTokenCall] = AuthApi.useRefreshTokenMutation()

    return (
        <Stack alignItems="center">
            <div>{translation.t("test")}</div>
            <Button
                onClick={() => {
                    refreshTokenCall().then(response => {
                        if (response.data) {
                            dispatch(AuthSlice.actions.setToken(response.data.accessToken))
                        }
                    })
                }}
            >
                Test event
            </Button>
        </Stack>
    )
}
