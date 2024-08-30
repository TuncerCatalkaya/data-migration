import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, InputAdornment, Paper, PaperProps, TextField } from "@mui/material"
import Draggable from "react-draggable"
import { Add, Close, Folder } from "@mui/icons-material"
import { ChangeEvent, useState } from "react"
import { ProjectApi } from "../../../../features/project/project.api"
import { useSnackbar } from "notistack"

interface CreateProjectDialogProps {
    open: boolean
    handleClickClose: () => void
}

function PaperComponent(props: PaperProps) {
    return (
        <Draggable handle="#create-project-dialog" cancel={'[class*="MuiDialogContent-root"]'}>
            <Paper {...props} />
        </Draggable>
    )
}

export default function CreateProjectDialog(createProjectDialogProps: Readonly<CreateProjectDialogProps>) {
    const [projectName, setProjectName] = useState<string>("")
    const [createProject] = ProjectApi.useCreateProjectMutation()
    const { enqueueSnackbar } = useSnackbar()

    const handleChangeProjectName = (e: ChangeEvent<HTMLInputElement>) => {
        setProjectName(e.target.value)
    }

    const handleClickCreateProject = async () => {
        const response = await createProject({ projectName })

        if (response.data) {
            enqueueSnackbar("Created project", { variant: "success" })
            createProjectDialogProps.handleClickClose()
            setProjectName("")
        } else if (response.error) {
            enqueueSnackbar("Something went wrong during project creation", { variant: "error" })
        }
    }

    return (
        <Dialog
            open={createProjectDialogProps.open}
            onClose={createProjectDialogProps.handleClickClose}
            aria-labelledby="create-project-dialog"
            PaperComponent={PaperComponent}
        >
            <DialogTitle sx={{ cursor: "move" }}>Create a new Project</DialogTitle>
            <DialogContent>
                <Box component="form" noValidate autoComplete="off">
                    <TextField
                        autoFocus
                        margin="dense"
                        label="Project name"
                        onChange={handleChangeProjectName}
                        fullWidth
                        variant="outlined"
                        inputProps={{ maxLength: 100 }}
                        InputProps={{
                            startAdornment: (
                                <InputAdornment position="start">
                                    <Folder />
                                </InputAdornment>
                            )
                        }}
                    />
                </Box>
            </DialogContent>
            <DialogActions>
                <Button variant="contained" color="error" onClick={createProjectDialogProps.handleClickClose} startIcon={<Close />}>
                    Cancel
                </Button>
                <Button variant="contained" disabled={!projectName} onClick={handleClickCreateProject} endIcon={<Add />}>
                    Create
                </Button>
            </DialogActions>
        </Dialog>
    )
}
