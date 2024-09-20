import { styled, Tab, Tabs } from "@mui/material"
import { useEffect, useMemo, useState } from "react"
import { generatePath, useLocation, useParams } from "react-router-dom"
import ProjectTabsStyles from "./ProjectTabs.styles"
import useNavigate from "../../../../router/hooks/useNavigate"
import RouterPaths from "../../../../router/constants/RouterPaths"

const CustomTabs = styled(Tabs)(() => ({
    "& .MuiTabs-indicator": {
        height: "0px"
    }
}))

const CustomTab = styled(Tab)(({ theme }) => ({
    textTransform: "none",
    backgroundColor: theme.palette.common.white,
    transition: "background-color 0.3s ease, color 0.3s ease",
    "&.Mui-selected": {
        backgroundColor: "#f5f5f5"
    },
    "&:hover": {
        color: theme.palette.primary.light
    },
    "&:not(.Mui-selected)": {
        transition: "background-color 0.3s ease"
    }
}))

export default function ProjectTabs() {
    const { projectId } = useParams()

    const { toProjectDetails, toProjectImport, toProjectMappedItems } = useNavigate()
    const location = useLocation()

    const allowedTabs = useMemo(
        () => [
            generatePath(RouterPaths.PROJECT_DETAILS, { projectId }),
            generatePath(RouterPaths.PROJECT_IMPORT, { projectId }),
            generatePath(RouterPaths.PROJECT_MAPPED_ITEMS, { projectId })
        ],
        [projectId]
    )
    const [tab, setTab] = useState<string>(allowedTabs[0])

    const handleClickToProjectDetailsTab = () => toProjectDetails(projectId!)
    const handleClickToProjectImportTab = () => toProjectImport(projectId!)
    const handleClickToProjectMappedItemsTab = () => toProjectMappedItems(projectId!)

    useEffect(() => {
        if (allowedTabs.includes(location.pathname)) {
            setTab(location.pathname)
        }
    }, [location, allowedTabs])

    return (
        <CustomTabs value={tab} variant="scrollable" scrollButtons="auto" sx={ProjectTabsStyles.tabs}>
            <CustomTab value={allowedTabs[0]} label={"Project Details"} onClick={handleClickToProjectDetailsTab} disableRipple />
            <CustomTab value={allowedTabs[1]} label={"Import"} onClick={handleClickToProjectImportTab} disableRipple />
            <CustomTab value={allowedTabs[2]} label={"Mapped items"} onClick={handleClickToProjectMappedItemsTab} disableRipple />
        </CustomTabs>
    )
}
