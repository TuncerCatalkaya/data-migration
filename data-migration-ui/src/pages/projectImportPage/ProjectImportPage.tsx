import {
    Alert,
    Box,
    Button,
    CircularProgress,
    FormControl,
    InputLabel,
    LinearProgress,
    MenuItem,
    Select,
    SelectChangeEvent,
    Stack,
    Tooltip,
    Typography
} from "@mui/material"
import { useParams } from "react-router-dom"
import { ChangeEvent, useCallback, useEffect, useState } from "react"
import { ProjectsApi } from "../../features/projects/projects.api"
import { useSnackbar } from "notistack"
import FileBrowserDialog from "../projectPage/components/dialogs/FileBrowserDialog"
import { Add, Bolt, Cloud, Delete, Edit, FileDownload, Transform } from "@mui/icons-material"
import { GetCurrentCheckpointStatusResponse, ItemResponse, MappingResponse, ScopeResponse } from "../../features/projects/projects.types"
import usePagination from "../../components/pagination/hooks/usePagination"
import ItemsTable from "./components/itemsTable/ItemsTable"
import theme from "../../theme"
import { ColDef } from "ag-grid-community"
import useConfirmationDialog from "../../components/confirmationDialog/hooks/useConfirmationDialog"
import ConfirmationDialog from "../../components/confirmationDialog/ConfirmationDialog"
import { useAppDispatch, useAppSelector } from "../../store/store"
import ScopeSlice, { ScopeMap } from "../../features/scope/scope.slice"
import GenerateScopeKey from "../../utils/GenerateScopeKey"
import GetFrontendEnvironment from "../../utils/GetFrontendEnvironment"
import { FetchBaseQueryError } from "@reduxjs/toolkit/query"
import ImportDataDialog from "./components/importDataDialog/ImportDataDialog"
import CreateMappingDialog from "./components/createMappingDialog/CreateMappingDialog"

export default function ProjectImportPage() {
    const { projectId } = useParams()
    const scopesFromStore = useAppSelector<ScopeMap>(state => state.scope.scopes)
    const dispatch = useAppDispatch()

    const [openImportDataDialog, setOpenImportDataDialog] = useState(false)

    const [openCreateMappingDialog, setOpenCreateMappingDialog] = useState(false)
    const [isMappingEditMode, setIsMappingEditMode] = useState(false)

    const [openFileBrowserDialog, setOpenFileBrowserDialog] = useState(false)

    const [scope, setScope] = useState(scopesFromStore[projectId!] || "select")
    const [scopesResponse, setScopesResponse] = useState<ScopeResponse[]>([])

    const [mapping, setMapping] = useState("select")
    const [mappingsResponse, setMappingsResponse] = useState<MappingResponse[]>([])

    const {
        openConfirmationDialog: openDeleteConfirmationDialog,
        handleClickCloseConfirmationDialog: handleClickCloseDeleteConfirmationDialog,
        handleClickOpenConfirmationDialog: handleClickOpenDeleteConfirmationDialog
    } = useConfirmationDialog()
    const {
        openConfirmationDialog: openInterruptConfirmationDialog,
        handleClickCloseConfirmationDialog: handleClickCloseInterruptConfirmationDialog,
        handleClickOpenConfirmationDialog: handleClickOpenInterruptConfirmationDialog
    } = useConfirmationDialog()
    const {
        openConfirmationDialog: openMappingDeleteConfirmationDialog,
        handleClickCloseConfirmationDialog: handleClickCloseMappingDeleteConfirmationDialog,
        handleClickOpenConfirmationDialog: handleClickOpenMappingDeleteConfirmationDialog
    } = useConfirmationDialog()

    const pagination = usePagination()
    const page = pagination.page
    const pageSize = pagination.pageSize
    const sort = pagination.sort
    const setTotalElements = pagination.setTotalElements

    const [createOrGetScope] = ProjectsApi.useCreateOrGetScopeMutation()
    const [importDataFile] = ProjectsApi.useImportDataFileMutation()
    const [importDataS3] = ProjectsApi.useImportDataS3Mutation()
    const [getScopes] = ProjectsApi.useLazyGetScopesQuery()
    const [getScopeHeaders] = ProjectsApi.useLazyGetScopeHeadersQuery()
    const [getItems] = ProjectsApi.useLazyGetItemsQuery()
    const [getMappings] = ProjectsApi.useLazyGetMappingsQuery()
    const [interruptScope] = ProjectsApi.useInterruptScopeMutation()
    const [markScopeForDeletion] = ProjectsApi.useMarkScopeForDeletionMutation()
    const [markMappingForDeletion] = ProjectsApi.useMarkMappingForDeletionMutation()
    const [getCurrentCheckpointStatus] = ProjectsApi.useLazyGetCurrentCheckpointStatusQuery()

    const { enqueueSnackbar } = useSnackbar()

    const handleClickOpenImportDataDialog = () => setOpenImportDataDialog(true)
    const handleClickCloseImportDataDialog = () => setOpenImportDataDialog(false)

    const handleClickOpenCreateMappingDialog = () => {
        setIsMappingEditMode(false)
        setOpenCreateMappingDialog(true)
    }
    const handleClickOpenEditMappingDialog = () => {
        setIsMappingEditMode(true)
        setOpenCreateMappingDialog(true)
    }
    const handleClickCloseCreateMappingDialog = async (shouldReload = false) => {
        setOpenCreateMappingDialog(false)
        if (shouldReload) {
            const getMappingsResponse = await getMappings({ projectId: projectId!, scopeId: scope }).unwrap()
            setMappingsResponse(getMappingsResponse)
        }
    }

    const handleClickOpenFileBrowserDialog = () => setOpenFileBrowserDialog(true)
    const handleClickCloseFileBrowserDialog = () => setOpenFileBrowserDialog(false)

    const handleFileChange = async (e: ChangeEvent<HTMLInputElement>, delimiter: string) => {
        if (delimiter === "select") {
            enqueueSnackbar("Please select a delimiter.", { variant: "error" })
        } else {
            const files = e.target.files
            if (files) {
                const file = files[0]
                e.target.value = ""
                if (!file.name.toLowerCase().endsWith(".csv")) {
                    enqueueSnackbar("Please select a CSV file.", { variant: "error" })
                } else {
                    const scopeKey = GenerateScopeKey(file)
                    const scopeResponse = await createOrGetScope({ projectId: projectId!, scopeKey, external: false }).unwrap()
                    await fetchScopesData()
                    setScope(scopeResponse.id)
                    dispatch(ScopeSlice.actions.addScope({ projectId: projectId!, scope: scopeResponse.id }))
                    setShouldStartTimer(true)
                    await importDataFile({ projectId: projectId!, scopeId: scopeResponse.id, delimiter, file })
                    enqueueSnackbar("Started data import process", { variant: "success" })
                }
            }
        }
        e.target.value = ""
    }

    const handleClickStartImportS3 = async (key: string) => {
        const scopeKey = key.split("/")[1]
        const scopeResponse = await createOrGetScope({
            projectId: projectId!,
            scopeKey,
            external: true
        }).unwrap()
        await fetchScopesData()
        handleClickCloseFileBrowserDialog()
        setScope(scopeResponse.id)
        dispatch(ScopeSlice.actions.addScope({ projectId: projectId!, scope: scopeResponse.id }))
        setShouldStartTimer(true)
        const bucket = GetFrontendEnvironment("VITE_S3_BUCKET")
        await importDataS3({ scopeId: scopeResponse.id, bucket, key })
        enqueueSnackbar("Started data import process", { variant: "success" })
    }

    const [currentCheckpointStatus, setCurrentCheckpointStatus] = useState<GetCurrentCheckpointStatusResponse>()
    const [shouldStartTimer, setShouldStartTimer] = useState(false)

    const handleScopeChange = async (event: SelectChangeEvent) => {
        const newScope = event.target.value
        setScope(newScope)
        dispatch(ScopeSlice.actions.addScope({ projectId: projectId!, scope: newScope }))
    }

    const selectedMapping = mappingsResponse.find(m => m.id === mapping)
    const handleMappingChange = async (event: SelectChangeEvent) => {
        const newMapping = event.target.value
        setMapping(newMapping)
    }

    const [rowData, setRowData] = useState<ItemResponse[]>([])
    const [scopeHeaders, setScopeHeaders] = useState<string[]>([])
    const [columnDefs, setColumnDefs] = useState<ColDef[]>([])

    const handleClickInterruptScope = async () => await interruptScope({ projectId: projectId!, scopeId: scope })

    const handleClickDeleteScope = async () => {
        await markScopeForDeletion({ projectId: projectId!, scopeId: scope })
        await fetchScopesData()
        setScope("select")
        setColumnDefs([])
        setRowData([])
        setTotalElements(0)
        setCurrentCheckpointStatus(undefined)
    }

    const handleClickDeleteMapping = async () => {
        await markMappingForDeletion({ projectId: projectId!, mappingId: mapping })
        await fetchMappingsData(scope)
        setMapping("select")
    }

    const fetchItemsData = useCallback(
        async (scopeId: string, page: number, pageSize: number, sort?: string) => {
            const getScopeHeadersResponse = await getScopeHeaders({ projectId: projectId!, scopeId }).unwrap()
            setScopeHeaders(getScopeHeadersResponse)
            const getItemsResponse = await getItems({ projectId: projectId!, scopeId, page, size: pageSize, sort }).unwrap()
            setRowData(getItemsResponse.content)
            setTotalElements(getItemsResponse.totalElements)
            await fetchMappingsData(scopeId)
            setMapping("select")
        },
        [getItems, setTotalElements, projectId, getScopeHeaders, getMappings]
    )

    const fetchMappingsData = useCallback(
        async (scopeId: string) => {
            const getMappingsResponse = await getMappings({ projectId: projectId!, scopeId }).unwrap()
            setMappingsResponse(getMappingsResponse)
        },
        [projectId, getMappings]
    )

    const fetchScopesData = useCallback(async () => {
        const scopesResponse = await getScopes({ projectId: projectId! }).unwrap()
        setScopesResponse(scopesResponse)
    }, [projectId, getScopes])

    useEffect(() => {
        fetchScopesData()
    }, [fetchScopesData])

    const fetchCurrentCheckpointStatus = useCallback(async () => {
        const statusResponse = await getCurrentCheckpointStatus({ projectId: projectId!, scopeId: scope })
        if (statusResponse.error) {
            const statusResponseError = statusResponse.error as FetchBaseQueryError
            setCurrentCheckpointStatus(undefined)
            if (statusResponseError.status === 404) {
                await fetchScopesData()
                setScope("select")
                setColumnDefs([])
                setRowData([])
                setTotalElements(0)
            }
        } else if (statusResponse.data) {
            const statusResponseData = statusResponse.data
            setCurrentCheckpointStatus(statusResponseData)

            if (statusResponseData.finished) {
                await fetchItemsData(scope, page, pageSize, sort)
            } else {
                setColumnDefs([])
                setRowData([])
                setTotalElements(0)
            }
        }
    }, [getCurrentCheckpointStatus, projectId, scope, fetchItemsData, page, pageSize, sort, setTotalElements, fetchScopesData])

    useEffect(() => {
        if (scope !== "select") {
            fetchCurrentCheckpointStatus()
        }
    }, [fetchCurrentCheckpointStatus, scope])

    useEffect(() => {
        let intervalId: number | null = null

        if (
            !currentCheckpointStatus?.finished &&
            scope !== "select" &&
            (currentCheckpointStatus?.processing || currentCheckpointStatus?.batchesProcessed === -1 || shouldStartTimer)
        ) {
            intervalId = setInterval(async () => {
                const statusResponse = await getCurrentCheckpointStatus({ projectId: projectId!, scopeId: scope })
                if (statusResponse.error) {
                    const statusResponseError = statusResponse.error as FetchBaseQueryError
                    if (statusResponseError.status === 404) {
                        if (intervalId) {
                            clearInterval(intervalId)
                            setShouldStartTimer(false)
                            await fetchScopesData()
                            setScope("select")
                            setColumnDefs([])
                            setRowData([])
                            setTotalElements(0)
                            setCurrentCheckpointStatus(undefined)
                            enqueueSnackbar("Error occurred during data import process. Scope got deleted.", { variant: "error" })
                        }
                    }
                } else if (statusResponse.data) {
                    const statusResponseData = statusResponse.data
                    setCurrentCheckpointStatus(statusResponseData)
                    if (!statusResponseData.processing && (statusResponseData.batchesProcessed !== -1 || statusResponseData.finished)) {
                        if (intervalId) {
                            clearInterval(intervalId)
                            setShouldStartTimer(false)
                            if (statusResponseData.finished) {
                                enqueueSnackbar("Data successfully imported", { variant: "success" })
                                await fetchItemsData(scope, page, pageSize, sort)
                            }
                        }
                    }
                }
            }, GetFrontendEnvironment("VITE_CURRENT_CHECKPOINT_INTERVAL_IN_MS"))
        }

        return () => {
            if (intervalId) {
                clearInterval(intervalId)
            }
        }
    }, [
        scope,
        currentCheckpointStatus,
        projectId,
        getCurrentCheckpointStatus,
        shouldStartTimer,
        page,
        pageSize,
        sort,
        fetchItemsData,
        fetchScopesData,
        setTotalElements
    ])

    return (
        <>
            {openImportDataDialog && (
                <ImportDataDialog open={openImportDataDialog} handleClickClose={handleClickCloseImportDataDialog} handleFileChange={handleFileChange} />
            )}
            {openCreateMappingDialog && (
                <CreateMappingDialog
                    open={openCreateMappingDialog}
                    handleClickClose={handleClickCloseCreateMappingDialog}
                    scopeId={scope}
                    mappingToEdit={isMappingEditMode ? selectedMapping : undefined}
                />
            )}
            {openFileBrowserDialog && (
                <FileBrowserDialog
                    open={openFileBrowserDialog}
                    handleClickClose={handleClickCloseFileBrowserDialog}
                    projectId={projectId!}
                    handleClickStartImportS3={handleClickStartImportS3}
                />
            )}
            {openInterruptConfirmationDialog && (
                <ConfirmationDialog
                    open={openInterruptConfirmationDialog}
                    handleClickClose={handleClickCloseInterruptConfirmationDialog}
                    handleClickYes={handleClickInterruptScope}
                >
                    <Stack spacing={2}>
                        <Typography variant="body1">Are you sure you want to interrupt the import of the scope?</Typography>
                    </Stack>
                </ConfirmationDialog>
            )}
            {openDeleteConfirmationDialog && (
                <ConfirmationDialog
                    open={openDeleteConfirmationDialog}
                    handleClickClose={handleClickCloseDeleteConfirmationDialog}
                    handleClickYes={handleClickDeleteScope}
                >
                    <Stack spacing={2}>
                        <Typography variant="body1">Are you sure you want to delete the scope?</Typography>
                    </Stack>
                </ConfirmationDialog>
            )}
            {openMappingDeleteConfirmationDialog && (
                <ConfirmationDialog
                    open={openMappingDeleteConfirmationDialog}
                    handleClickClose={handleClickCloseMappingDeleteConfirmationDialog}
                    handleClickYes={handleClickDeleteMapping}
                >
                    <Stack spacing={2}>
                        <Typography variant="body1">Are you sure you want to delete the mapping?</Typography>
                    </Stack>
                </ConfirmationDialog>
            )}
            <Stack spacing={2} width="100vw">
                <Stack spacing={2} justifyContent="space-between" direction="row">
                    <Tooltip title={scopesResponse.find(s => s.id === scope)?.key} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                        <FormControl sx={{ backgroundColor: theme.palette.common.white, minWidth: 425, maxWidth: 425, textAlign: "left" }}>
                            <InputLabel>Scope</InputLabel>
                            <Select value={scope} label="Scope" onChange={handleScopeChange}>
                                <MenuItem value="select" disabled>
                                    {"Select a scope / import data"}
                                </MenuItem>
                                {scopesResponse.map(scope => (
                                    <MenuItem key={scope.id} value={scope.id}>
                                        {scope.key}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </Tooltip>
                    <Stack direction="row" spacing={2}>
                        <Box>
                            <Button
                                disabled={
                                    scope === "select" ||
                                    currentCheckpointStatus?.finished ||
                                    (!currentCheckpointStatus?.processing && currentCheckpointStatus?.batchesProcessed !== -1)
                                }
                                variant="contained"
                                color="warning"
                                onClick={handleClickOpenInterruptConfirmationDialog}
                                endIcon={<Bolt />}
                                sx={{ color: theme.palette.common.white }}
                            >
                                Interrupt
                            </Button>
                        </Box>
                        <Box>
                            <Button
                                disabled={
                                    scope === "select" ||
                                    currentCheckpointStatus?.processing ||
                                    (!currentCheckpointStatus?.finished && currentCheckpointStatus?.batchesProcessed === -1)
                                }
                                variant="contained"
                                color="error"
                                onClick={handleClickOpenDeleteConfirmationDialog}
                                endIcon={<Delete />}
                            >
                                Delete
                            </Button>
                        </Box>
                    </Stack>
                </Stack>
                <Stack spacing={2} justifyContent="space-between" direction="row" alignItems="center">
                    <Stack direction="row" spacing={2}>
                        <Box sx={{ ml: "auto" }}>
                            <Button variant="contained" startIcon={<FileDownload />} onClick={handleClickOpenImportDataDialog}>
                                Import small file
                            </Button>
                        </Box>
                        <Box sx={{ ml: "auto" }}>
                            <Button color="secondary" variant="contained" startIcon={<Cloud />} onClick={handleClickOpenFileBrowserDialog}>
                                Import large files
                            </Button>
                        </Box>
                    </Stack>
                    <Stack direction="row" spacing={1} sx={{ display: rowData.length === 0 ? "none" : "display" }}>
                        <Tooltip title={"Apply selected mapping to selected items"} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                            <Button disabled color="info" variant="contained" onClick={handleClickOpenCreateMappingDialog}>
                                <Transform />
                            </Button>
                        </Tooltip>
                        <Tooltip title={mappingsResponse.find(m => m.id === mapping)?.name} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                            <FormControl sx={{ backgroundColor: theme.palette.common.white, minWidth: 200, maxWidth: 200, textAlign: "left" }}>
                                <InputLabel>Mapping</InputLabel>
                                <Select value={mapping} label="Mapping" onChange={handleMappingChange}>
                                    <MenuItem value="select" disabled>
                                        {"Select a mapping"}
                                    </MenuItem>
                                    {mappingsResponse.map(mapping => (
                                        <MenuItem key={mapping.id} value={mapping.id}>
                                            {mapping.name}
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                        </Tooltip>
                        <Button variant="contained" color="success" onClick={handleClickOpenCreateMappingDialog} sx={{ color: theme.palette.common.white }}>
                            <Add />
                        </Button>
                        <Button
                            disabled={mapping === "select"}
                            variant="contained"
                            color="warning"
                            onClick={handleClickOpenEditMappingDialog}
                            sx={{ color: theme.palette.common.white }}
                        >
                            <Edit />
                        </Button>
                        <Button disabled={mapping === "select"} variant="contained" color="error" onClick={handleClickOpenMappingDeleteConfirmationDialog}>
                            <Delete />
                        </Button>
                    </Stack>
                    {currentCheckpointStatus && (
                        <Stack
                            direction="row"
                            alignItems="center"
                            spacing={2}
                            sx={{
                                border: "1px solid " + theme.palette.divider,
                                borderRadius: "8px",
                                padding: "5px",
                                background: theme.palette.common.white,
                                width: "30%"
                            }}
                        >
                            <Stack alignItems="center" sx={{ width: "100%" }}>
                                {currentCheckpointStatus?.finished && <Alert>Data is imported and available</Alert>}
                                {!currentCheckpointStatus?.finished && currentCheckpointStatus.totalBatches !== 0 && (
                                    <>
                                        {currentCheckpointStatus.batchesProcessed !== -1 && (
                                            <Typography>
                                                {currentCheckpointStatus?.batchesProcessed}/{currentCheckpointStatus?.totalBatches} Batches
                                            </Typography>
                                        )}
                                        {currentCheckpointStatus.batchesProcessed === -1 && <Typography>{"Starting import..."}</Typography>}
                                        <Box sx={{ width: "100%" }}>
                                            <LinearProgress
                                                variant="determinate"
                                                value={
                                                    currentCheckpointStatus && currentCheckpointStatus.batchesProcessed !== -1
                                                        ? (currentCheckpointStatus?.batchesProcessed / currentCheckpointStatus?.totalBatches) * 100
                                                        : 0
                                                }
                                                sx={{ height: "10px", borderRadius: "8px" }}
                                            />
                                        </Box>
                                    </>
                                )}
                            </Stack>
                            {(currentCheckpointStatus?.processing ||
                                (!currentCheckpointStatus.finished && currentCheckpointStatus.batchesProcessed === -1)) && <CircularProgress size={30} />}
                            {!currentCheckpointStatus?.processing &&
                                !currentCheckpointStatus?.finished &&
                                currentCheckpointStatus?.external &&
                                currentCheckpointStatus.batchesProcessed !== -1 && (
                                    <Alert severity="warning" sx={{ width: "100%" }}>
                                        Manually restart import
                                    </Alert>
                                )}
                            {!currentCheckpointStatus?.processing &&
                                !currentCheckpointStatus?.finished &&
                                !currentCheckpointStatus?.external &&
                                currentCheckpointStatus.batchesProcessed !== -1 && (
                                    <Alert severity="error" sx={{ width: "100%" }}>
                                        Manually delete scope
                                    </Alert>
                                )}
                        </Stack>
                    )}
                </Stack>
                <ItemsTable rowData={rowData} scopeHeaders={scopeHeaders} columnDefs={columnDefs} setColumnDefs={setColumnDefs} {...pagination} />
            </Stack>
        </>
    )
}
