import {
    Button,
    Dialog,
    DialogContent,
    DialogTitle,
    FormControl,
    InputLabel,
    MenuItem,
    Paper,
    PaperProps,
    Select,
    SelectChangeEvent,
    Stack,
    Typography
} from "@mui/material"
import Draggable from "react-draggable"
import theme from "../../../../theme"
import { useTranslation } from "react-i18next"
import { Add, Delete, Edit, Transform } from "@mui/icons-material"
import { useCallback, useEffect, useState } from "react"
import CreateHostDialog from "../createHostDialog/CreateHostDialog"
import { HostsApi } from "../../../../features/hosts/hosts.api"
import { Host } from "../../../../features/hosts/hosts.types"

interface FileBrowserDialogProps {
    open: boolean
    handleClickClose: () => void
}

function PaperComponent(props: PaperProps) {
    return (
        <Draggable handle="#create-mapping-dialog" cancel={'[class*="MuiDialogContent-root"]'}>
            <Paper {...props} />
        </Draggable>
    )
}

export default function CreateMappingDialog({ open, handleClickClose }: Readonly<FileBrowserDialogProps>) {
    const [host, setHost] = useState("select")
    const [database, setDatabase] = useState("select")
    const [hostsResponse, setHostsResponse] = useState<Host[]>([])

    const [openCreateHostDialog, setOpenCreateHostDialog] = useState(false)

    const [getHosts] = HostsApi.useLazyGetHostsQuery()

    const translation = useTranslation()

    const handleClickOpenCreateHostDialog = () => setOpenCreateHostDialog(true)
    const handleClickCloseCreateHostDialog = () => setOpenCreateHostDialog(false)

    const selectedHost = hostsResponse.find(h => h.id === host)
    const handleHostChange = async (event: SelectChangeEvent) => {
        const newHost = event.target.value
        setHost(newHost)
        setDatabase("select")
    }

    const handleDatabaseChange = async (event: SelectChangeEvent) => {
        const newDatabase = event.target.value
        setDatabase(newDatabase)
    }

    const fetchHostsData = useCallback(async () => {
        const hostsResponse = await getHosts().unwrap()
        setHostsResponse(hostsResponse)
    }, [getHosts])

    useEffect(() => {
        fetchHostsData()
    }, [fetchHostsData])

    return (
        <>
            {openCreateHostDialog && <CreateHostDialog open={openCreateHostDialog} handleClickClose={handleClickCloseCreateHostDialog} />}
            <Dialog
                open={open}
                onClose={handleClickClose}
                aria-labelledby="create-mapping-dialog"
                PaperComponent={PaperComponent}
                sx={{ zIndex: theme.zIndex.modal }}
            >
                <DialogTitle sx={{ cursor: "move" }}>
                    <Stack spacing={1}>
                        <Stack direction="row" display="flex" justifyContent="space-between">
                            <Stack direction="row" alignItems="center" spacing={1}>
                                <Transform />
                                <Typography variant="h6">{translation.t("pages.projectImport.components.dialogs.createMappingDialog.title")}</Typography>
                            </Stack>
                        </Stack>
                    </Stack>
                </DialogTitle>
                <DialogContent>
                    <Paper sx={{ padding: "25px" }}>
                        <Stack direction="row" alignItems="center" spacing={2} sx={{ padding: "5px" }}>
                            <Stack spacing={2}>
                                <Stack direction="row" spacing={2}>
                                    <FormControl required sx={{ backgroundColor: theme.palette.common.white, minWidth: "200px", maxWidth: "200px" }}>
                                        <InputLabel>Host</InputLabel>
                                        <Select value={host} label="host" onChange={handleHostChange}>
                                            <MenuItem value="select" disabled>
                                                {"Select a host"}
                                            </MenuItem>
                                            {hostsResponse.map(host => (
                                                <MenuItem key={host.id} value={host.id}>
                                                    {host.name}
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                    <Stack direction="row" spacing={1}>
                                        <Button
                                            variant="contained"
                                            color="success"
                                            sx={{ color: theme.palette.common.white }}
                                            onClick={handleClickOpenCreateHostDialog}
                                        >
                                            <Add />
                                        </Button>
                                        <Button variant="contained" color="warning" sx={{ color: theme.palette.common.white }}>
                                            <Edit />
                                        </Button>
                                        <Button variant="contained" color="error">
                                            <Delete />
                                        </Button>
                                    </Stack>
                                </Stack>
                                <FormControl required sx={{ backgroundColor: theme.palette.common.white, minWidth: "200px", maxWidth: "200px" }}>
                                    <InputLabel>Database</InputLabel>
                                    <Select value={database} label="database" onChange={handleDatabaseChange}>
                                        <MenuItem value="select" disabled>
                                            {"Select a database"}
                                        </MenuItem>
                                        {selectedHost?.databases?.map(database => (
                                            <MenuItem key={database.id} value={database.id}>
                                                {database.name}
                                            </MenuItem>
                                        ))}
                                    </Select>
                                </FormControl>
                            </Stack>
                        </Stack>
                    </Paper>
                </DialogContent>
            </Dialog>
        </>
    )
}
