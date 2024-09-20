import { Button, Stack } from "@mui/material"
import { useParams } from "react-router-dom"
import { ChangeEvent, useState } from "react"
import { ProjectsApi } from "../../features/projects/projects.api"
import { useSnackbar } from "notistack"
import FileBrowserDialog from "../projectPage/components/dialogs/FileBrowserDialog"
import { Cloud, FileDownload } from "@mui/icons-material"
import { VisuallyHiddenInput } from "../../components/visuallyHiddenInput/VisuallyHiddenInput"

export default function ProjectImportPage() {
    const { projectId } = useParams()

    const [openFileBrowserDialog, setOpenFileBrowserDialog] = useState(false)

    const [importDataFile] = ProjectsApi.useImportDataFileMutation()

    const { enqueueSnackbar } = useSnackbar()

    const handleClickOpenFileBrowserDialog = () => setOpenFileBrowserDialog(true)
    const handleClickCloseFileBrowserDialog = (shouldReload = false) => {
        setOpenFileBrowserDialog(false)
        console.log(shouldReload)
        // if (shouldReload) {
        //     fetchData(page, pageSize, sort)
        // }
    }

    const handleFileChange = async (e: ChangeEvent<HTMLInputElement>) => {
        const files = e.target.files
        if (files) {
            const file = files[0]
            e.target.value = ""
            if (!file.name.toLowerCase().endsWith(".csv")) {
                enqueueSnackbar("Please select a CSV file.", { variant: "error" })
            } else {
                await importDataFile({ projectId: projectId!, file })
            }
        }
        e.target.value = ""
    }

    return (
        <>
            {openFileBrowserDialog && (
                <FileBrowserDialog open={openFileBrowserDialog} handleClickClose={handleClickCloseFileBrowserDialog} projectId={projectId!} />
            )}
            <Stack spacing={2}>
                <Stack>{projectId}</Stack>
                <Button component="label" role={undefined} variant="contained" tabIndex={-1} startIcon={<FileDownload />}>
                    Import small file
                    <VisuallyHiddenInput type="file" accept=".csv" onChange={handleFileChange} />
                </Button>
                <Button color="secondary" variant="contained" startIcon={<Cloud />} onClick={handleClickOpenFileBrowserDialog}>
                    Import large files
                </Button>
            </Stack>
        </>
    )
}
