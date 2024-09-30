import {
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
    Stack,
    TextField,
    Typography
} from "@mui/material"
import Draggable from "react-draggable"
import theme from "../../../../theme"
import { useTranslation } from "react-i18next"
import { Add, Delete, Edit, Transform } from "@mui/icons-material"
import { ChangeEvent, useCallback, useEffect, useState } from "react"
import CreateOrEditHostDialog from ".././createOrEditHostDialog/CreateOrEditHostDialog"
import { HostsApi } from "../../../../features/hosts/hosts.api"
import { Host } from "../../../../features/hosts/hosts.types"
import useConfirmationDialog from "../../../../components/confirmationDialog/hooks/useConfirmationDialog"
import ConfirmationDialog from "../../../../components/confirmationDialog/ConfirmationDialog"
import { ProjectsApi } from "../../../../features/projects/projects.api"
import { useParams } from "react-router-dom"
import { InputField } from "../../../../components/addableCard/AddableCard.types"
import { v4 as uuidv4 } from "uuid"

interface CreateMappingDialogProps {
    open: boolean
    handleClickClose: () => void
    scopeId: string
}

function PaperComponent(props: PaperProps) {
    return (
        <Draggable handle="#create-mapping-dialog" cancel={'[class*="MuiDialogContent-root"]'}>
            <Paper {...props} />
        </Draggable>
    )
}

export default function CreateMappingDialog({ open, handleClickClose, scopeId }: Readonly<CreateMappingDialogProps>) {
    const { projectId } = useParams()
    const [host, setHost] = useState("select")
    const [database, setDatabase] = useState("select")
    const [mappingName, setMappingName] = useState("")
    const [hostsResponse, setHostsResponse] = useState<Host[]>([])
    const [mappings, setMappings] = useState<InputField[]>([])

    const [openCreateOrEditHostDialog, setOpenCreateOrEditHostDialog] = useState(false)
    const [isEditMode, setIsEditMode] = useState(false)

    const [getHosts] = HostsApi.useLazyGetHostsQuery()
    const [deleteHost] = HostsApi.useDeleteHostMutation()
    const [getScopeHeaders] = ProjectsApi.useLazyGetScopeHeadersQuery()

    const { openConfirmationDialog, handleClickCloseConfirmationDialog, handleClickOpenConfirmationDialog } = useConfirmationDialog()

    const translation = useTranslation()

    const handleClickOpenCreateHostDialog = () => {
        setIsEditMode(false)
        setOpenCreateOrEditHostDialog(true)
    }
    const handleClickOpenEditHostDialog = () => {
        setIsEditMode(true)
        setOpenCreateOrEditHostDialog(true)
    }
    const handleClickCloseCreateOrEditHostDialog = async (shouldReload = false) => {
        setOpenCreateOrEditHostDialog(false)
        if (shouldReload) {
            await fetchHostsData()
        }
    }

    const handleClickDeleteHost = async () => {
        await deleteHost({ hostId: selectedHost!.id })
        await fetchHostsData()
        setHost("select")
        setDatabase("select")
    }

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

    const handleMappingNameChange = async (event: ChangeEvent<HTMLInputElement>) => {
        const newMappingName = event.target.value
        setMappingName(newMappingName)
    }

    const handleMappingChange = (id: string, value: string): void => {
        const updatedMappings = mappings.map(mapping => {
            if (mapping.id === id) {
                return { ...mapping, value }
            }
            return mapping
        })
        setMappings(updatedMappings)
    }

    const handleClickSubmit = async () => {
        // const host: Host = {
        //     id: hostToEdit?.id ?? "",
        //     name: hostName,
        //     url: hostUrl,
        //     databases: databases.map(database => {
        //         return {
        //             id: database.dbId,
        //             name: database.value
        //         }
        //     })
        // }
        // const createOrUpdateHostResponse = await createOrUpdateHost(host)
        // if (createOrUpdateHostResponse.data) {
        //     handleClickClose(true)
        // } else if (createOrUpdateHostResponse.error) {
        //     const createOrUpdateHostResponseError = createOrUpdateHostResponse.error as FetchBaseQueryError
        //     if (createOrUpdateHostResponseError.status === 409) {
        //         setHostUrlError("Host URL already exists")
        //     }
        // }
    }

    const submitButtonDisabled = host === "select" || database === "select" || mappingName.trim() === ""

    const fetchScopeHeadersData = useCallback(async () => {
        const getScopeHeadersResponse = await getScopeHeaders({ projectId: projectId!, scopeId }).unwrap()
        setMappings(
            getScopeHeadersResponse.map(scopeHeader => ({
                id: uuidv4(),
                dbId: "",
                value: scopeHeader,
                label: scopeHeader
            }))
        )
    }, [getScopeHeaders])

    useEffect(() => {
        fetchScopeHeadersData()
    }, [fetchScopeHeadersData])

    const fetchHostsData = useCallback(async () => {
        const hostsResponse = await getHosts().unwrap()
        setHostsResponse(hostsResponse)
    }, [getHosts])

    useEffect(() => {
        fetchHostsData()
    }, [fetchHostsData])

    return (
        <>
            {openCreateOrEditHostDialog && (
                <CreateOrEditHostDialog
                    open={openCreateOrEditHostDialog}
                    handleClickClose={handleClickCloseCreateOrEditHostDialog}
                    hostToEdit={isEditMode ? selectedHost : undefined}
                />
            )}
            {openConfirmationDialog && (
                <ConfirmationDialog open={openConfirmationDialog} handleClickClose={handleClickCloseConfirmationDialog} handleClickYes={handleClickDeleteHost}>
                    <Stack spacing={2}>
                        <Typography variant="body1">Are you sure you want to delete the host?</Typography>
                        <Stack>
                            <Typography variant="body1">
                                {"Host name: "} <strong>{selectedHost!.name}</strong>
                            </Typography>
                            <Typography variant="body1">
                                {"Host URL: "} <strong>{selectedHost!.url}</strong>
                            </Typography>
                        </Stack>
                    </Stack>
                </ConfirmationDialog>
            )}
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
                    <Stack spacing={2}>
                        <Paper sx={{ padding: "25px" }}>
                            <Typography variant="h6" sx={{ paddingBottom: "15px" }}>
                                Select a target system
                            </Typography>
                            <Stack direction="row" alignItems="center" spacing={2} sx={{ padding: "5px" }}>
                                <Stack spacing={2}>
                                    <Stack direction="row" spacing={2}>
                                        <FormControl sx={{ backgroundColor: theme.palette.common.white, minWidth: "200px", maxWidth: "200px" }}>
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
                                                onClick={handleClickOpenCreateHostDialog}
                                                sx={{ color: theme.palette.common.white }}
                                            >
                                                <Add />
                                            </Button>
                                            <Button
                                                disabled={!selectedHost}
                                                variant="contained"
                                                color="warning"
                                                onClick={handleClickOpenEditHostDialog}
                                                sx={{ color: theme.palette.common.white }}
                                            >
                                                <Edit />
                                            </Button>
                                            <Button disabled={!selectedHost} variant="contained" color="error" onClick={handleClickOpenConfirmationDialog}>
                                                <Delete />
                                            </Button>
                                        </Stack>
                                    </Stack>
                                    <FormControl sx={{ backgroundColor: theme.palette.common.white, minWidth: "200px", maxWidth: "200px" }}>
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
                        <Paper sx={{ padding: "25px" }}>
                            <Typography variant="h6" sx={{ paddingBottom: "15px" }}>
                                Decide a name for the mapping
                            </Typography>
                            <TextField label="Name" value={mappingName} onChange={handleMappingNameChange} />
                        </Paper>
                        <Paper sx={{ padding: "25px" }}>
                            <Typography variant="h6" sx={{ paddingBottom: "15px" }}>
                                Mapping of headers
                            </Typography>
                            <Stack spacing={2}>
                                {mappings.map(mapping => (
                                    <TextField
                                        key={mapping.id}
                                        label={mapping.label}
                                        value={mapping.value}
                                        onChange={e => handleMappingChange(mapping.id, e.target.value)}
                                    />
                                ))}
                            </Stack>
                        </Paper>
                    </Stack>
                </DialogContent>
                <DialogActions>
                    <Button variant="contained" disabled={submitButtonDisabled} onClick={handleClickSubmit}>
                        Submit
                    </Button>
                    <Button variant="contained" color="error" onClick={() => handleClickClose()}>
                        Cancel
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    )
}
