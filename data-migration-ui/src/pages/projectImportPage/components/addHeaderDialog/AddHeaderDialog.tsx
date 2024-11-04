import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, InputAdornment, Paper, PaperProps, TextField } from "@mui/material"
import Draggable from "react-draggable"
import { Add, Close, ViewColumn } from "@mui/icons-material"
import { ChangeEvent, useState } from "react"
import { ProjectsApi } from "../../../../features/projects/projects.api"
import { useSnackbar } from "notistack"
import theme from "../../../../theme"
import { useParams } from "react-router-dom"

interface AddHeaderDialogProps {
    open: boolean
    handleClickClose: (shouldReload?: boolean) => void
    scopeId: string
}

function PaperComponent(props: PaperProps) {
    return (
        <Draggable handle="#add-header-dialog" cancel={'[class*="MuiDialogContent-root"]'}>
            <Paper {...props} />
        </Draggable>
    )
}

export default function AddHeaderDialog(addHeaderDialogProps: Readonly<AddHeaderDialogProps>) {
    const { projectId } = useParams()
    const [extraHeader, setExtraHeader] = useState<string>("")
    const [addExtraHeader] = ProjectsApi.useAddExtraHeaderMutation()
    const { enqueueSnackbar } = useSnackbar()

    const closeDialog = (shouldReload = false) => {
        addHeaderDialogProps.handleClickClose(shouldReload)
        setExtraHeader("")
    }

    const handleClickAddHeader = async () => {
        if (extraHeader.trim() === "") {
            enqueueSnackbar("Header is not allowed to be empty", { variant: "error" })
            return
        }
        const response = await addExtraHeader({ projectId: projectId!, scopeId: addHeaderDialogProps.scopeId, extraHeader })
        if (response.error) {
            enqueueSnackbar("Header already exists", { variant: "error" })
        } else {
            enqueueSnackbar("Added header", { variant: "success" })
            closeDialog(true)
        }
    }

    const handleChangeHeader = (e: ChangeEvent<HTMLInputElement>) => setExtraHeader(e.target.value)

    const handleAddHeaderKeyPress = async (event: React.KeyboardEvent<HTMLInputElement>) => {
        if (event.key === "Enter") {
            event.preventDefault()
            if (extraHeader) {
                await handleClickAddHeader()
            }
        }
    }

    return (
        <Dialog
            open={addHeaderDialogProps.open}
            onClose={() => closeDialog()}
            aria-labelledby="add-header-dialog"
            PaperComponent={PaperComponent}
            sx={{ zIndex: theme.zIndex.modal }}
        >
            <DialogTitle sx={{ cursor: "move" }}>{"Add Header"}</DialogTitle>
            <DialogContent>
                <Box component="form" noValidate autoComplete="off">
                    <TextField
                        autoFocus
                        margin="dense"
                        value={extraHeader}
                        label={"Header"}
                        onChange={handleChangeHeader}
                        onKeyDown={handleAddHeaderKeyPress}
                        fullWidth
                        variant="outlined"
                        InputProps={{
                            startAdornment: (
                                <InputAdornment position="start">
                                    <ViewColumn />
                                </InputAdornment>
                            )
                        }}
                    />
                </Box>
            </DialogContent>
            <DialogActions>
                <Button variant="contained" color="error" onClick={() => closeDialog()} startIcon={<Close />}>
                    {"Cancel"}
                </Button>
                <Button variant="contained" disabled={!extraHeader} onClick={handleClickAddHeader} endIcon={<Add />}>
                    {"Add"}
                </Button>
            </DialogActions>
        </Dialog>
    )
}
