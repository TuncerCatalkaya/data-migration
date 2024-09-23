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
    Typography
} from "@mui/material"
import { useParams } from "react-router-dom"
import { ChangeEvent, useCallback, useEffect, useState } from "react"
import { ProjectsApi } from "../../features/projects/projects.api"
import { useSnackbar } from "notistack"
import FileBrowserDialog from "../projectPage/components/dialogs/FileBrowserDialog"
import { Bolt, Cloud, Delete, FileDownload } from "@mui/icons-material"
import { GetCurrentCheckpointStatusResponse, ItemResponse, ScopeResponse } from "../../features/projects/projects.types"
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

export default function ProjectImportPage() {
    const { projectId } = useParams()
    const scopesFromStore = useAppSelector<ScopeMap>(state => state.scope.scopes)
    const dispatch = useAppDispatch()

    const [openImportDataDialog, setOpenImportDataDialog] = useState(false)
    const [openFileBrowserDialog, setOpenFileBrowserDialog] = useState(false)

    const [scopeResponse, setScopeResponse] = useState<ScopeResponse[]>([])

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

    const pagination = usePagination()
    const page = pagination.page
    const pageSize = pagination.pageSize
    const sort = pagination.sort
    const setTotalElements = pagination.setTotalElements

    const [createOrGetScope] = ProjectsApi.useCreateOrGetScopeMutation()
    const [importDataFile] = ProjectsApi.useImportDataFileMutation()
    const [importDataS3] = ProjectsApi.useImportDataS3Mutation()
    const [getScopes] = ProjectsApi.useLazyGetScopesQuery()
    const [getItems] = ProjectsApi.useLazyGetItemsQuery()
    const [interruptScope] = ProjectsApi.useInterruptScopeMutation()
    const [deleteScope] = ProjectsApi.useDeleteScopeMutation()
    const [getCurrentCheckpointStatus] = ProjectsApi.useLazyGetCurrentCheckpointStatusQuery()

    const { enqueueSnackbar } = useSnackbar()

    const handleClickOpenImportDataDialog = () => setOpenImportDataDialog(true)
    const handleClickCloseImportDataDialog = () => setOpenImportDataDialog(false)

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
    }

    const [scope, setScope] = useState(scopesFromStore[projectId!] || "select")

    const [currentCheckpointStatus, setCurrentCheckpointStatus] = useState<GetCurrentCheckpointStatusResponse>()
    const [shouldStartTimer, setShouldStartTimer] = useState(false)

    const handleScopeChange = async (event: SelectChangeEvent) => {
        const newScope = event.target.value
        setScope(newScope)
        dispatch(ScopeSlice.actions.addScope({ projectId: projectId!, scope: newScope }))
    }

    const [rowData, setRowData] = useState<ItemResponse[]>([])
    const [columnDefs, setColumnDefs] = useState<ColDef[]>([])

    const handleClickInterruptScope = async () => await interruptScope({ projectId: projectId!, scopeId: scope })

    const handleClickDeleteScope = async () => {
        await deleteScope({ projectId: projectId!, scopeId: scope })
        await fetchScopesData()
        setScope("select")
        setColumnDefs([])
        setRowData([])
        setTotalElements(0)
        setCurrentCheckpointStatus(undefined)
    }

    const fetchItemsData = useCallback(
        async (scopeId: string, page: number, pageSize: number, sort?: string) => {
            const response = await getItems({ projectId: projectId!, scopeId, page, size: pageSize, sort }).unwrap()
            setRowData(response.content)
            setTotalElements(response.totalElements)
        },
        [getItems, setTotalElements, projectId]
    )

    const fetchCurrentCheckpointStatus = useCallback(async () => {
        const statusResponse = await getCurrentCheckpointStatus({ projectId: projectId!, scopeId: scope })
        if (statusResponse.error) {
            const statusResponseError = statusResponse.error as FetchBaseQueryError
            if (statusResponseError.status === 404) {
                await fetchScopesData()
                setScope("select")
                setColumnDefs([])
                setRowData([])
                setTotalElements(0)
                setCurrentCheckpointStatus(undefined)
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
    }, [getCurrentCheckpointStatus, projectId, scope, fetchItemsData, page, pageSize, sort, setTotalElements])

    const fetchScopesData = useCallback(async () => {
        const response = await getScopes({ projectId: projectId! })
        if (response.data) {
            setScopeResponse(response.data)
        }
    }, [projectId, getScopes])

    useEffect(() => {
        fetchScopesData()
    }, [fetchScopesData])

    useEffect(() => {
        if (scope !== "select") {
            fetchCurrentCheckpointStatus()
        }
    }, [fetchCurrentCheckpointStatus, scope])

    useEffect(() => {
        let intervalId: number | null = null

        if (!currentCheckpointStatus?.finished && scope !== "select" && (currentCheckpointStatus?.processing || shouldStartTimer)) {
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
                        }
                    }
                } else if (statusResponse.data) {
                    const statusResponseData = statusResponse.data
                    setCurrentCheckpointStatus(statusResponseData)
                    if (!statusResponseData.processing) {
                        if (intervalId) {
                            clearInterval(intervalId)
                            setShouldStartTimer(false)
                            if (statusResponseData.finished) {
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
    }, [scope, currentCheckpointStatus, projectId, getCurrentCheckpointStatus, shouldStartTimer, page, pageSize, sort, fetchItemsData])

    return (
        <>
            {openImportDataDialog && (
                <ImportDataDialog open={openImportDataDialog} handleClickClose={handleClickCloseImportDataDialog} handleFileChange={handleFileChange} />
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
            <Stack spacing={2} width="100vw">
                <Stack spacing={2} direction="row">
                    <FormControl sx={{ backgroundColor: theme.palette.common.white, width: "fit-content" }}>
                        <InputLabel>Scope</InputLabel>
                        <Select value={scope} label="Scope" onChange={handleScopeChange}>
                            <MenuItem value="select" disabled>
                                {"Select a scope"}
                            </MenuItem>
                            {scopeResponse.map(scope => (
                                <MenuItem key={scope.id} value={scope.id}>
                                    {scope.key}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                    <Box sx={{ flexGrow: 1 }} />
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
                <Stack spacing={2} direction="row" alignItems="center">
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
                    <Box sx={{ flexGrow: 1 }} />
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
                                {!currentCheckpointStatus?.finished && (
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
                <ItemsTable rowData={rowData} columnDefs={columnDefs} setColumnDefs={setColumnDefs} {...pagination} />
            </Stack>
        </>
    )
}
