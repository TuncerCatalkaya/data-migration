import { useCallback, useEffect, useState } from "react"
import { Box, Button, Stack } from "@mui/material"
import CreateProjectDialog from "./components/dialogs/CreateProjectDialog"
import { Add } from "@mui/icons-material"
import { ProjectsApi } from "../../features/projects/projects.api"
import { ProjectInformationResponse } from "../../features/projects/projects.types"
import ProjectsTable from "./components/projectsTable/ProjectsTable"
import usePagination from "../../components/pagination/hooks/usePagination"

export default function ProjectsPage() {
    const [openCreateProjectDialog, setOpenCreateProjectDialog] = useState(false)
    const [projects] = ProjectsApi.useLazyGetProjectsQuery()
    const pagination = usePagination()
    const page = pagination.page
    const pageSize = pagination.pageSize
    const sort = pagination.sort
    const setTotalElements = pagination.setTotalElements

    const handleClickOpenCreateProjectDialog = () => setOpenCreateProjectDialog(true)
    const handleClickCloseCreateProjectDialog = (shouldReload = false) => {
        setOpenCreateProjectDialog(false)
        if (shouldReload) {
            fetchData(page, pageSize, sort)
        }
    }

    const [rowData, setRowData] = useState<ProjectInformationResponse[]>([])

    const fetchData = useCallback(
        (page: number, pageSize: number, sort?: string) => {
            projects({ page: page, size: pageSize, sort })
                .unwrap()
                .then(response => {
                    setRowData(response.content)
                    setTotalElements(response.totalElements)
                })
        },
        [projects, setTotalElements]
    )

    useEffect(() => {
        fetchData(page, pageSize, sort)
    }, [fetchData, page, pageSize, sort])

    return (
        <>
            <CreateProjectDialog open={openCreateProjectDialog} handleClickClose={handleClickCloseCreateProjectDialog} />
            <Stack spacing={2}>
                <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
                    <Button variant="contained" onClick={handleClickOpenCreateProjectDialog} endIcon={<Add />}>
                        Create Project
                    </Button>
                </Box>
                <ProjectsTable rowData={rowData} {...pagination} />
            </Stack>
        </>
    )
}
