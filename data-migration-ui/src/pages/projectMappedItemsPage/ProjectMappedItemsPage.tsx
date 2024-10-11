import { Box, Button, FormControl, InputLabel, MenuItem, Select, SelectChangeEvent, Stack, Tooltip, Typography } from "@mui/material"
import { useParams } from "react-router-dom"
import { ChangeEvent, useCallback, useEffect, useState } from "react"
import { ProjectsApi } from "../../features/projects/projects.api"
import { useSnackbar } from "notistack"
import { Delete } from "@mui/icons-material"
import { MappedItemResponse, MappingResponse, ScopeResponse } from "../../features/projects/projects.types"
import usePagination from "../../components/pagination/hooks/usePagination"
import theme from "../../theme"
import { ColDef } from "ag-grid-community"
import useConfirmationDialog from "../../components/confirmationDialog/hooks/useConfirmationDialog"
import ConfirmationDialog from "../../components/confirmationDialog/ConfirmationDialog"
import MappedItemsTable from "./components/mappedItemsTable/MappedItemsTable"

export default function ProjectMappedItemsPage() {
    const { projectId } = useParams()

    const [openCreateMappingDialog, setOpenCreateMappingDialog] = useState(false)
    const [isMappingEditMode, setIsMappingEditMode] = useState(false)

    const [scope, setScope] = useState("select")
    const [scopesResponse, setScopesResponse] = useState<ScopeResponse[]>([])

    const [mapping, setMapping] = useState("select")
    const [mappingsResponse, setMappingsResponse] = useState<MappingResponse[]>([])

    const [selectedItems, setSelectedItems] = useState<string[]>([])

    const [checkedFilterMappedItems, setCheckedFilterMappedItems] = useState(false)

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

    const [getScopes] = ProjectsApi.useLazyGetScopesQuery()
    const [getScopeHeaders] = ProjectsApi.useLazyGetScopeHeadersQuery()
    const [getItems] = ProjectsApi.useLazyGetItemsQuery()
    const [getMappings] = ProjectsApi.useLazyGetMappingsQuery()
    const [getMappedItems] = ProjectsApi.useLazyGetMappedItemsQuery()
    const [markMappingForDeletion] = ProjectsApi.useMarkMappingForDeletionMutation()

    const { enqueueSnackbar } = useSnackbar()

    const handleClickOpenCreateMappingDialog = () => {
        setIsMappingEditMode(false)
        setOpenCreateMappingDialog(true)
    }
    const handleClickOpenEditMappingDialog = () => {
        setIsMappingEditMode(true)
        setOpenCreateMappingDialog(true)
    }

    const handleFilterMappedItemsChange = (e: ChangeEvent<HTMLInputElement>) => setCheckedFilterMappedItems(e.target.checked)

    const selectedScope = scopesResponse.find(s => s.id === scope)
    const handleScopeChange = async (event: SelectChangeEvent) => {
        const newScope = event.target.value
        setScope(newScope)
        if (selectedMapping) {
            setMapping("select")
            setColumnDefs([])
            setRowData([])
            setTotalElements(0)
        }
    }

    const selectedMapping = mappingsResponse.find(m => m.id === mapping)
    const handleMappingChange = async (event: SelectChangeEvent) => {
        const newMapping = event.target.value
        setMapping(newMapping)
    }

    const [rowData, setRowData] = useState<MappedItemResponse[]>([])
    const [scopeHeaders, setScopeHeaders] = useState<string[]>([])
    const [columnDefs, setColumnDefs] = useState<ColDef[]>([])

    const handleClickDeleteMapping = async () => {
        await markMappingForDeletion({ projectId: projectId!, mappingId: mapping })
        await fetchMappingsData(selectedScope!.id)
        setMapping("select")
        setColumnDefs([])
        setRowData([])
        setTotalElements(0)
    }

    const fetchMappedItemsData = useCallback(
        async (mappingId: string, scopeId: string, page: number, pageSize: number, sort?: string) => {
            const getScopeHeadersResponse = await getScopeHeaders({ projectId: projectId!, scopeId }).unwrap()
            setScopeHeaders(getScopeHeadersResponse)
            const getMappedItemsResponse = await getMappedItems({
                projectId: projectId!,
                mappingId: mappingId,
                page,
                size: pageSize,
                sort
            }).unwrap()
            setRowData(getMappedItemsResponse.content)
            setTotalElements(getMappedItemsResponse.totalElements)
        },
        [getItems, setTotalElements, projectId, getScopeHeaders, getMappings, mapping, checkedFilterMappedItems]
    )

    useEffect(() => {
        if (selectedMapping && selectedScope) {
            fetchMappedItemsData(selectedMapping.id, selectedScope.id, page, pageSize, sort)
        }
    }, [fetchMappedItemsData])

    const fetchScopesData = useCallback(async () => {
        const getScopesResponse = await getScopes({ projectId: projectId! }).unwrap()
        setScopesResponse(getScopesResponse)
    }, [projectId, getScopes])

    useEffect(() => {
        fetchScopesData()
    }, [fetchScopesData])

    const fetchMappingsData = useCallback(
        async (scopeId: string) => {
            const getMappingsResponse = await getMappings({ projectId: projectId!, scopeId }).unwrap()
            setMappingsResponse(getMappingsResponse)
        },
        [projectId, getMappings]
    )

    useEffect(() => {
        if (selectedScope) {
            fetchMappingsData(selectedScope.id)
        }
    }, [selectedScope, fetchMappingsData])

    return (
        <>
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
                    <Stack spacing={2} direction="row">
                        <Tooltip title={selectedScope?.key} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                            <FormControl sx={{ backgroundColor: theme.palette.common.white, minWidth: 425, maxWidth: 425, textAlign: "left" }}>
                                <InputLabel>Scope</InputLabel>
                                <Select value={scope} label="Scope" onChange={handleScopeChange}>
                                    <MenuItem value="select" disabled>
                                        {"Select a scope"}
                                    </MenuItem>
                                    {scopesResponse.map(scope => (
                                        <MenuItem key={scope.id} value={scope.id}>
                                            {scope.key}
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                        </Tooltip>
                        <Tooltip title={selectedMapping?.name} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                            <FormControl sx={{ backgroundColor: theme.palette.common.white, minWidth: 425, maxWidth: 425, textAlign: "left" }}>
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
                    </Stack>
                    <Box>
                        <Button
                            disabled={!selectedMapping}
                            variant="contained"
                            color="error"
                            onClick={handleClickOpenMappingDeleteConfirmationDialog}
                            endIcon={<Delete />}
                        >
                            Delete
                        </Button>
                    </Box>
                </Stack>
                <MappedItemsTable
                    rowData={rowData}
                    scopeHeaders={scopeHeaders}
                    selectedScope={selectedScope}
                    selectedMapping={selectedMapping}
                    columnDefs={columnDefs}
                    setColumnDefs={setColumnDefs}
                    setSelectedItems={setSelectedItems}
                    mapping={mapping}
                    fetchMappedItemsData={fetchMappedItemsData}
                    {...pagination}
                />
            </Stack>
        </>
    )
}
