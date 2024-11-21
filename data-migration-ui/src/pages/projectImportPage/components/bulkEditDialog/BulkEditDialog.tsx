import {
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    InputAdornment,
    InputLabel,
    MenuItem,
    Paper,
    PaperProps,
    Select,
    SelectChangeEvent,
    TextField,
    Tooltip
} from "@mui/material"
import Draggable from "react-draggable"
import { Check, Close, ViewColumn } from "@mui/icons-material"
import { ChangeEvent, useState } from "react"
import { ProjectsApi } from "../../../../features/projects/projects.api"
import { useSnackbar } from "notistack"
import theme from "../../../../theme"
import { useParams } from "react-router-dom"

interface BulkEditDialogProps {
    open: boolean
    handleClickClose: (shouldReload?: boolean) => void
    itemIds: string[]
    headers: string[]
}

function PaperComponent(props: PaperProps) {
    return (
        <Draggable handle="#bulk-edit-dialog" cancel={'[class*="MuiDialogContent-root"]'}>
            <Paper {...props} />
        </Draggable>
    )
}

export default function BulkEditDialog(bulkEditDialogProps: Readonly<BulkEditDialogProps>) {
    const { projectId } = useParams()
    const [header, setHeader] = useState("select")
    const [newValue, setNewValue] = useState<string>("")
    const [updateItemProperties] = ProjectsApi.useUpdateItemPropertiesMutation()
    const { enqueueSnackbar } = useSnackbar()

    const closeDialog = (shouldReload = false) => {
        bulkEditDialogProps.handleClickClose(shouldReload)
        setHeader("select")
    }

    const handleClickBulkEdit = async () => {
        const response = await updateItemProperties({ projectId: projectId!, itemIds: bulkEditDialogProps.itemIds, key: header, newValue })
        if (response.error) {
            enqueueSnackbar("Error occurred during bulk edit", { variant: "error" })
        } else {
            enqueueSnackbar("Bulk edited column for selected items", { variant: "success" })
            closeDialog(true)
        }
    }

    const handleChangeNewValue = (e: ChangeEvent<HTMLInputElement>) => setNewValue(e.target.value)

    const handleChangeHeader = async (event: SelectChangeEvent) => setHeader(event.target.value)

    const handleNewValueKeyPress = async (event: React.KeyboardEvent<HTMLInputElement>) => {
        if (event.key === "Enter") {
            event.preventDefault()
            if (header !== "select" && newValue) {
                // backend call
            }
        }
    }

    return (
        <Dialog
            open={bulkEditDialogProps.open}
            onClose={() => closeDialog()}
            aria-labelledby="bulk-edit-dialog"
            PaperComponent={PaperComponent}
            sx={{ zIndex: theme.zIndex.modal }}
        >
            <DialogTitle sx={{ cursor: "move" }}>{"Bulk edit currently shown items of a column"}</DialogTitle>
            <DialogContent>
                <Box margin={1}>
                    <Tooltip title={header} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                        <FormControl sx={{ backgroundColor: theme.palette.common.white, minWidth: 425, maxWidth: 425, textAlign: "left" }}>
                            <InputLabel>Header</InputLabel>
                            <Select value={header} label="Header" onChange={handleChangeHeader}>
                                <MenuItem value="select" disabled>
                                    {"Select a header"}
                                </MenuItem>
                                {bulkEditDialogProps.headers.map(header => (
                                    <MenuItem key={header} value={header}>
                                        {header}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </Tooltip>
                    <Box component="form" noValidate autoComplete="off">
                        <TextField
                            autoFocus
                            margin="dense"
                            value={newValue}
                            label={"New Value"}
                            onChange={handleChangeNewValue}
                            onKeyDown={handleNewValueKeyPress}
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
                </Box>
            </DialogContent>
            <DialogActions>
                <Button variant="contained" color="error" onClick={() => closeDialog()} startIcon={<Close />}>
                    {"Cancel"}
                </Button>
                <Button variant="contained" disabled={header === "select" || !newValue} onClick={handleClickBulkEdit} endIcon={<Check />}>
                    {"Accept"}
                </Button>
            </DialogActions>
        </Dialog>
    )
}
