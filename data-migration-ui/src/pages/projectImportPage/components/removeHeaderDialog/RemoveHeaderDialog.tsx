import {
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    InputLabel,
    MenuItem,
    Paper,
    PaperProps,
    Select,
    SelectChangeEvent,
    Tooltip
} from "@mui/material"
import Draggable from "react-draggable"
import { Close, Delete } from "@mui/icons-material"
import { useState } from "react"
import { ProjectsApi } from "../../../../features/projects/projects.api"
import { useSnackbar } from "notistack"
import theme from "../../../../theme"
import { useParams } from "react-router-dom"

interface RemoveHeaderDialogProps {
    open: boolean
    handleClickClose: (shouldReload?: boolean) => void
    scopeId: string
    extraHeaders: string[]
}

function PaperComponent(props: PaperProps) {
    return (
        <Draggable handle="#remove-header-dialog" cancel={'[class*="MuiDialogContent-root"]'}>
            <Paper {...props} />
        </Draggable>
    )
}

export default function RemoveHeaderDialog(removeHeaderDialogProps: Readonly<RemoveHeaderDialogProps>) {
    const { projectId } = useParams()
    const [extraHeader, setExtraHeader] = useState("select")
    const [removeExtraHeader] = ProjectsApi.useRemoveExtraHeaderMutation()
    const { enqueueSnackbar } = useSnackbar()

    const handleExtraHeaderChange = async (event: SelectChangeEvent) => setExtraHeader(event.target.value)

    const closeDialog = (shouldReload = false) => {
        removeHeaderDialogProps.handleClickClose(shouldReload)
        setExtraHeader("select")
    }

    const handleClickRemoveHeader = async () => {
        const response = await removeExtraHeader({ projectId: projectId!, scopeId: removeHeaderDialogProps.scopeId, extraHeader })
        if (response.error) {
            enqueueSnackbar("Error occurred, header is could not be deleted", { variant: "error" })
        } else {
            enqueueSnackbar("Removed header", { variant: "success" })
            closeDialog(true)
        }
    }

    return (
        <Dialog
            open={removeHeaderDialogProps.open}
            onClose={() => closeDialog()}
            aria-labelledby="remove-header-dialog"
            PaperComponent={PaperComponent}
            sx={{ zIndex: theme.zIndex.modal }}
        >
            <DialogTitle sx={{ cursor: "move" }}>{"Remove Header"}</DialogTitle>
            <DialogContent>
                <Box margin={1}>
                    <Tooltip title={extraHeader} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                        <FormControl sx={{ backgroundColor: theme.palette.common.white, minWidth: 425, maxWidth: 425, textAlign: "left" }}>
                            <InputLabel>Header</InputLabel>
                            <Select value={extraHeader} label="Header" onChange={handleExtraHeaderChange}>
                                <MenuItem value="select" disabled>
                                    {"Select a header"}
                                </MenuItem>
                                {removeHeaderDialogProps.extraHeaders.map(extraHeader => (
                                    <MenuItem key={extraHeader} value={extraHeader}>
                                        {extraHeader}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </Tooltip>
                </Box>
            </DialogContent>
            <DialogActions>
                <Button variant="contained" color="secondary" onClick={() => closeDialog()} startIcon={<Close />}>
                    {"Cancel"}
                </Button>
                <Button variant="contained" color="error" disabled={extraHeader === "select"} onClick={handleClickRemoveHeader} endIcon={<Delete />}>
                    {"Remove"}
                </Button>
            </DialogActions>
        </Dialog>
    )
}
