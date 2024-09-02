import { Stack } from "@mui/material"
import { useParams } from "react-router-dom"

export default function ProjectPage() {
    const { projectId } = useParams()
    return <Stack>{projectId}</Stack>
}
