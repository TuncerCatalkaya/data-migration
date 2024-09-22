import { Box, Button, FormControl, InputLabel, MenuItem, Select, SelectChangeEvent, Stack, Typography } from "@mui/material"
import { useParams } from "react-router-dom"
import { ChangeEvent, useCallback, useEffect, useState } from "react"
import { ProjectsApi } from "../../features/projects/projects.api"
import { useSnackbar } from "notistack"
import FileBrowserDialog from "../projectPage/components/dialogs/FileBrowserDialog"
import { Cloud, Delete, FileDownload } from "@mui/icons-material"
import { VisuallyHiddenInput } from "../../components/visuallyHiddenInput/VisuallyHiddenInput"
import { ItemResponse, ScopeResponse } from "../../features/projects/projects.types"
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

export default function ProjectImportPage() {
    const { projectId } = useParams()
    const scopesFromStore = useAppSelector<ScopeMap>(state => state.scope.scopes)
    const delimitersFromStore = useAppSelector<ScopeMap>(state => state.scope.delimiters)
    const dispatch = useAppDispatch()

    const [openFileBrowserDialog, setOpenFileBrowserDialog] = useState(false)

    const [scopeResponse, setScopeResponse] = useState<ScopeResponse[]>([])

    const { openConfirmationDialog, handleClickCloseConfirmationDialog, handleClickOpenConfirmationDialog } = useConfirmationDialog()

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
    const [deleteScope] = ProjectsApi.useDeleteScopeMutation()
    const [getCurrentCheckpointStatus] = ProjectsApi.useLazyGetCurrentCheckpointStatusQuery()

    const { enqueueSnackbar } = useSnackbar()

    const handleClickOpenFileBrowserDialog = () => {
        if (delimiter === "select") {
            enqueueSnackbar("Please select a delimiter.", { variant: "error" })
            return
        }
        setOpenFileBrowserDialog(true)
    }
    const handleClickCloseFileBrowserDialog = async (shouldReload = false) => {
        setOpenFileBrowserDialog(false)
        //await fetchScopesData()
        if (shouldReload) {
            //await fetchScopesData()
        }
    }

    const handleFileChange = async (e: ChangeEvent<HTMLInputElement>) => {
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
        const bucket = GetFrontendEnvironment("VITE_S3_BUCKET")
        await importDataS3({ scopeId: scopeResponse.id, bucket, key: key, delimiter })
    }

    const [scope, setScope] = useState(scopesFromStore[projectId!] || "select")
    const [delimiter, setDelimiter] = useState(delimitersFromStore[projectId!] || "select")

    const handleScopeChange = async (event: SelectChangeEvent) => {
        const newScope = event.target.value
        setScope(newScope)
        dispatch(ScopeSlice.actions.addScope({ projectId: projectId!, scope: newScope }))
        const response = await getCurrentCheckpointStatus({ projectId: projectId!, scopeId: newScope })
        console.log(response)
    }
    const handleDelimiterChange = async (event: SelectChangeEvent) => {
        const newDelimiter = event.target.value
        setDelimiter(newDelimiter)
        dispatch(ScopeSlice.actions.addDelimiter({ projectId: projectId!, delimiter: newDelimiter }))
    }

    const [rowData, setRowData] = useState<ItemResponse[]>([])
    const [columnDefs, setColumnDefs] = useState<ColDef[]>([])

    const handleClickDeleteScope = async () => {
        await deleteScope({ projectId: projectId!, scopeId: scope })
        await fetchScopesData()
        setScope("select")
        setColumnDefs([])
        setRowData([])
        setTotalElements(0)
    }

    const fetchItemsData = useCallback(
        async (scopeId: string, page: number, pageSize: number, sort?: string) => {
            const response = await getItems({ projectId: projectId!, scopeId, page, size: pageSize, sort }).unwrap()
            setRowData(response.content)
            setTotalElements(response.totalElements)
        },
        [getItems, setTotalElements, projectId]
    )

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
            fetchItemsData(scope, page, pageSize, sort)
        }
    }, [fetchItemsData, scope, page, pageSize, sort])

    return (
        <>
            {openFileBrowserDialog && (
                <FileBrowserDialog
                    open={openFileBrowserDialog}
                    handleClickClose={handleClickCloseFileBrowserDialog}
                    projectId={projectId!}
                    handleClickStartImportS3={handleClickStartImportS3}
                />
            )}
            {openConfirmationDialog && (
                <ConfirmationDialog open={openConfirmationDialog} handleClickClose={handleClickCloseConfirmationDialog} handleClickYes={handleClickDeleteScope}>
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
                    <Box sx={{ ml: "auto" }}>
                        <Button
                            disabled={scope === "select"}
                            variant="contained"
                            color="error"
                            onClick={handleClickOpenConfirmationDialog}
                            endIcon={<Delete />}
                        >
                            Delete
                        </Button>
                    </Box>
                </Stack>
                <Stack spacing={2} direction="row" alignItems="center">
                    <Box sx={{ ml: "auto" }}>
                        <Button component="label" role={undefined} variant="contained" tabIndex={-1} startIcon={<FileDownload />}>
                            Import small file
                            <VisuallyHiddenInput type="file" accept=".csv" onChange={handleFileChange} />
                        </Button>
                    </Box>
                    <Box sx={{ ml: "auto" }}>
                        <Button color="secondary" variant="contained" startIcon={<Cloud />} onClick={handleClickOpenFileBrowserDialog}>
                            Import large files
                        </Button>
                    </Box>
                    <FormControl sx={{ backgroundColor: theme.palette.common.white, width: "fit-content" }}>
                        <InputLabel>Delimiter</InputLabel>
                        <Select value={delimiter} label="delimiter" onChange={handleDelimiterChange}>
                            <MenuItem value="select" disabled>
                                {"Select a delimiter"}
                            </MenuItem>
                            <MenuItem value=",">comma (,)</MenuItem>
                            <MenuItem value=";">semicolon (;)</MenuItem>
                            <MenuItem value="\t">tab (\t)</MenuItem>
                            <MenuItem value="|">pipe (|)</MenuItem>
                            <MenuItem value=" ">space ()</MenuItem>
                        </Select>
                    </FormControl>
                </Stack>
                <ItemsTable rowData={rowData} columnDefs={columnDefs} setColumnDefs={setColumnDefs} {...pagination} />
            </Stack>
        </>
    )
}
