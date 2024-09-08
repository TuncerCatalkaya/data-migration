import { Button, Stack } from "@mui/material"
import { useParams } from "react-router-dom"
import { Cloud, FileDownload } from "@mui/icons-material"
import FileBrowserDialog from "./components/dialogs/FileBrowserDialog"
import { VisuallyHiddenInput } from "../../components/visuallyHiddenInput/VisuallyHiddenInput"
import { useState } from "react"

export default function ProjectPage() {
    const { projectId } = useParams()

    const [openFileBrowserDialog, setOpenFileBrowserDialog] = useState(false)

    const handleClickOpenFileBrowserDialog = () => setOpenFileBrowserDialog(true)
    const handleClickCloseFileBrowserDialog = (shouldReload = false) => {
        setOpenFileBrowserDialog(false)
        console.log(shouldReload)
        // if (shouldReload) {
        //     fetchData(page, pageSize, sort)
        // }
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
                    <VisuallyHiddenInput type="file" accept=".csv" />
                </Button>
                <Button color="secondary" variant="contained" startIcon={<Cloud />} onClick={handleClickOpenFileBrowserDialog}>
                    Import large files
                </Button>
            </Stack>
        </>
    )
}
