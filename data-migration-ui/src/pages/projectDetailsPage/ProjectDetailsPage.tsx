import { Stack } from "@mui/material"
import { useParams } from "react-router-dom"
import { useCallback, useEffect } from "react"
import { ProjectsApi } from "../../features/projects/projects.api"

export default function ProjectDetailsPage() {
    const { projectId } = useParams()
    const [getProject] = ProjectsApi.useLazyGetProjectQuery()

    const fetchData = useCallback(() => {
        getProject({ projectId: projectId! })
            .unwrap()
            .then(response => {
                console.log(response)
            })
    }, [getProject, projectId])

    useEffect(() => {
        fetchData()
    }, [fetchData])

    return (
        <>
            <Stack spacing={2}>
                <Stack>{projectId}</Stack>
            </Stack>
        </>
    )
}
