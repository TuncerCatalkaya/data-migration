import { HashRouter, Navigate, Route, Routes } from "react-router-dom"
import { RouterPaths } from "./constants/routerPaths"
import TokenProtectedRoute from "../components/protectedRoute/TokenProtectedRoute"
import RootPage from "../pages/rootPage/RootPage"
import ProjectsPage from "../pages/projectsPage/ProjectsPage"
import ProjectPage from "../pages/projectPage/ProjectPage"

export default function Router() {
    return (
        <HashRouter>
            <Routes>
                <Route path={RouterPaths.INDEX} element={<Navigate to={RouterPaths.ROOT} replace />} />

                <Route path={RouterPaths.ROOT} element={<TokenProtectedRoute component={<RootPage />} />}>
                    <Route index element={<Navigate to={RouterPaths.PROJECTS} replace />} />
                    <Route path={RouterPaths.PROJECTS} element={<ProjectsPage />} />
                    <Route path={RouterPaths.PROJECT} element={<ProjectPage />} />
                </Route>

                <Route path="*" element={<Navigate to={RouterPaths.INDEX} replace />} />
            </Routes>
        </HashRouter>
    )
}
