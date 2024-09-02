import "ag-grid-community/styles/ag-grid.css"
import "ag-grid-community/styles/ag-theme-alpine.css"
import { useCallback, useEffect, useState } from "react"
import { Box, Button, IconButton, Stack, TablePagination } from "@mui/material"
import "./ProjectsPage.css"
import CreateProjectDialog from "./components/dialogs/CreateProjectDialog"
import { Add, MoreVert } from "@mui/icons-material"
import { ProjectApi } from "../../features/project/project.api"
import { AgGridReact } from "ag-grid-react"
import { ColDef, ICellRendererParams } from "ag-grid-community"
import { ProjectInformationResponse } from "../../features/project/project.types"
import FormatDate from "../../utils/FormatDate"
import useNavigate from "../../router/hooks/useNavigate"

export default function ProjectsPage() {
    const navigate = useNavigate()
    const [openCreateProjectDialog, setOpenCreateProjectDialog] = useState(false)
    const [projects] = ProjectApi.useLazyGetProjectsQuery()
    const [pageSize, setPageSize] = useState(10)
    const [page, setPage] = useState(0)
    const [sort, setSort] = useState<string | undefined>(undefined)
    const [totalElements, setTotalElements] = useState(0)

    const handleClickOpenCreateProjectDialog = () => setOpenCreateProjectDialog(true)
    const handleClickCloseCreateProjectDialog = (shouldReload = false) => {
        setOpenCreateProjectDialog(false)
        if (shouldReload) {
            fetchData(page, pageSize, sort)
        }
    }

    const [rowData, setRowData] = useState<ProjectInformationResponse[]>([])

    const actionElement = (params: ICellRendererParams) => (
        <IconButton onClick={() => console.log(params.data)}>
            <MoreVert />
        </IconButton>
    )

    const [columnDefs] = useState<ColDef[]>([
        { field: "id", tooltipField: "id", maxWidth: 200 },
        { headerName: "Project name", field: "name", tooltipField: "name" },
        { field: "createdDate", maxWidth: 150, valueFormatter: params => FormatDate(params.value) },
        { headerName: "Last Updated", field: "lastUpdatedDate", maxWidth: 150, valueFormatter: params => FormatDate(params.value) },
        {
            headerName: "",
            field: "action",
            maxWidth: 80,
            sortable: false,
            onCellClicked: () => console.log("open menu"),
            cellRenderer: actionElement
        }
    ])

    const fetchData = useCallback(
        (page: number, pageSize: number, sort?: string) => {
            projects({ page: page, size: pageSize, sort })
                .unwrap()
                .then(response => {
                    setRowData(response.content)
                    setTotalElements(response.totalElements)
                })
        },
        [projects]
    )

    useEffect(() => {
        fetchData(page, pageSize, sort)
    }, [fetchData, page, pageSize, sort])

    return (
        <>
            <CreateProjectDialog open={openCreateProjectDialog} handleClickClose={handleClickCloseCreateProjectDialog} />
            <Stack spacing={2}>
                <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
                    <Button variant="contained" onClick={handleClickOpenCreateProjectDialog} endIcon={<Add />}>
                        Create Project
                    </Button>
                </Box>
                <Stack>
                    <div className="ag-theme-alpine" style={{ width: 900, height: 471, textAlign: "left" }}>
                        <AgGridReact
                            rowData={rowData}
                            columnDefs={columnDefs}
                            onSortChanged={params => {
                                const sort = params.api.getColumnState().find(s => s.sort != null)
                                setSort(sort ? sort.colId + "," + sort.sort : undefined)
                            }}
                            defaultColDef={{
                                flex: 1,
                                resizable: false,
                                comparator: () => 0
                            }}
                            tooltipShowDelay={1000}
                            tooltipInteraction
                            suppressMovableColumns
                            suppressCellFocus
                            suppressColumnMoveAnimation
                            animateRows={false}
                            suppressAnimationFrame
                            onCellClicked={e => {
                                if (e.colDef.field === "action") {
                                    return
                                }
                                navigate.toProject(e.data.id)
                            }}
                        />
                    </div>
                    <TablePagination
                        count={totalElements}
                        page={page}
                        rowsPerPage={pageSize}
                        showFirstButton
                        showLastButton
                        onPageChange={(_, page) => setPage(page)}
                        onRowsPerPageChange={e => {
                            const newPageSize = +e.target.value
                            const newPage = Math.floor((page * pageSize) / newPageSize)
                            setPage(newPage)
                            setPageSize(newPageSize)
                        }}
                        labelRowsPerPage="Rows per page:"
                        labelDisplayedRows={({ from, to, count }) => `${from}–${to} of ${count}`}
                        getItemAriaLabel={type => {
                            if (type === "first") {
                                return "Go to first page"
                            } else if (type === "previous") {
                                return "Go to previous page"
                            } else if (type === "next") {
                                return "Go to next page"
                            } else if (type === "last") {
                                return "Go to last page"
                            }
                            return ""
                        }}
                    />
                </Stack>
            </Stack>
        </>
    )
}
