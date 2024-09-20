import { Box, Button, capitalize, Stack, TextField, Typography } from "@mui/material"
import { useParams } from "react-router-dom"
import { useCallback, useEffect, useState } from "react"
import { ProjectsApi } from "../../features/projects/projects.api"
import { ProjectInformationResponse } from "../../features/projects/projects.types"
import { FetchBaseQueryError } from "@reduxjs/toolkit/query"
import useNavigate from "../../router/hooks/useNavigate"
import theme from "../../theme"
import { Edit } from "@mui/icons-material"
import FormatDate from "../../utils/FormatDate"

const excludedKeys = ["owner"]
const readOnlyKeys = ["id", "createdDate", "lastUpdatedDate"]
const dateKeys = ["createdDate", "lastUpdatedDate"]

export default function ProjectDetailsPage() {
    const { projectId } = useParams()
    const { toProjects } = useNavigate()
    const [projectInformationResponse, setProjectInformationResponse] = useState<Partial<ProjectInformationResponse>>({})
    const [changed, setChanged] = useState<boolean>(false)

    const [getProject] = ProjectsApi.useLazyGetProjectQuery()
    const [updateProject] = ProjectsApi.useUpdateProjectMutation()

    const fetchData = useCallback(async () => {
        const response = await getProject({ projectId: projectId! })
        if (response.data) {
            setProjectInformationResponse(response.data)
        } else if (response.error) {
            const responseError = response.error as FetchBaseQueryError
            if (responseError.status === 404) {
                toProjects()
            }
        }
    }, [getProject, projectId, toProjects])

    useEffect(() => {
        fetchData()
    }, [fetchData])

    const handleInputChange = (field: keyof ProjectInformationResponse, value: string) => {
        setChanged(true)
        setProjectInformationResponse(prevData => ({
            ...prevData,
            [field]: value
        }))
    }

    const handleUpdateProjectClick = async () => {
        await updateProject({ projectId: projectInformationResponse.id!, projectName: projectInformationResponse.name! })
        setChanged(false)
        await fetchData()
    }

    return (
        <Stack width="100vw">
            <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
                <Button disabled={!changed} variant="contained" onClick={handleUpdateProjectClick} endIcon={<Edit />}>
                    Save Changes
                </Button>
            </Box>
            <Stack spacing={2}>
                {Object.entries(projectInformationResponse)
                    .filter(([key]) => !excludedKeys.includes(key))
                    .map(([key, value]) => {
                        let formattedValue = value as string
                        if (dateKeys.includes(key)) {
                            formattedValue = FormatDate(value as Date)
                        }
                        return (
                            <Stack key={key}>
                                <Typography variant="h6">{capitalize(key)}:</Typography>
                                <TextField
                                    disabled={readOnlyKeys.includes(key)}
                                    value={formattedValue}
                                    onChange={e => handleInputChange(key as keyof ProjectInformationResponse, e.target.value)}
                                    sx={{ backgroundColor: theme.palette.common.white }}
                                />
                            </Stack>
                        )
                    })}
            </Stack>
        </Stack>
    )
}
