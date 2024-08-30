import { useAppSelector } from "../../store/store"
import TokenForm from "../../components/tokenform/TokenForm"
import { Outlet } from "react-router-dom"
import { AuthUtils } from "../../features/auth/auth.utils"
import ErrorContent from "../../components/errorContent/ErrorContent"
import DataMigrationSpinner from "../../components/dataMigrationSpinner/DataMigrationSpinner"
import { useTranslation } from "react-i18next"

export default function RootPage() {
    const token = useAppSelector(state => state.auth.token)
    const isBusy = useAppSelector<boolean>(state => state.busy.isBusy)

    const translation = useTranslation()

    const invalidTokenElement = import.meta.env.DEV
        ? !AuthUtils.isJwtTokenValid(token) && <TokenForm />
        : token && <ErrorContent h2={translation.t("pages.root.error.h2")} h5={translation.t("pages.root.error.h5")} />

    return (
        <>
            {isBusy && <DataMigrationSpinner />}
            {AuthUtils.isJwtTokenValid(token) ? <Outlet /> : invalidTokenElement}
        </>
    )
}
