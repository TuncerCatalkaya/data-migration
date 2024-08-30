import "ag-grid-community/styles/ag-grid.css"
import "ag-grid-community/styles/ag-theme-quartz.css"
import { useState } from "react"
import { Box, Button, Stack } from "@mui/material"
import "./ProjectsPage.css"
import CreateProjectDialog from "./components/dialogs/CreateProjectDialog"
import { Add } from "@mui/icons-material"

export default function ProjectsPage() {
    const [openCreateProjectDialog, setOpenCreateProjectDialog] = useState(false)

    const handleClickOpenCreateProjectDialog = () => setOpenCreateProjectDialog(true)
    const handleClickCloseCreateProjectDialog = () => setOpenCreateProjectDialog(false)

    // const [rowData, setRowData] = useState([
    //     {
    //         make: "Porscheaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
    //         model: "Boxter",
    //         price: 72000
    //     },
    //     { make: "Ford", model: "Mondeo", price: 32000 },
    //     { make: "Ford", model: "Mondeo", price: 32000 },
    //     { make: "Toyota", model: "Celica", price: 35000 }
    // ])
    //
    // const [columnDefs, setColumnDefs] = useState<ColDef[]>([
    //     { field: "make", tooltipField: "make", maxWidth: 300 },
    //     { field: "model", maxWidth: 300 },
    //     { field: "price", maxWidth: 300 }
    // ])

    // useEffect(() => {
    //     if (gridRef.current) {
    //         gridRef.current.api.autoSizeAllColumns()
    //     }
    // }, [rowData, columnDefs])

    return (
        <>
            <CreateProjectDialog open={openCreateProjectDialog} handleClickClose={handleClickCloseCreateProjectDialog} />
            <Stack spacing={2}>
                <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
                    <Button variant="contained" onClick={handleClickOpenCreateProjectDialog} endIcon={<Add />}>
                        Create Project
                    </Button>
                </Box>
                {/*<div className="ag-theme-quartz" style={{ width: 900 }}>*/}
                {/*    <AgGridReact*/}
                {/*        rowData={rowData}*/}
                {/*        columnDefs={columnDefs}*/}
                {/*        onSortChanged={params => console.log(params.api.getColumnState().find(s => s.sort != null))}*/}
                {/*        defaultColDef={{*/}
                {/*            flex: 1*/}
                {/*        }}*/}
                {/*        tooltipShowDelay={500}*/}
                {/*        tooltipInteraction*/}
                {/*        domLayout="autoHeight"*/}
                {/*        enableCellTextSelection*/}
                {/*    />*/}
                {/*</div>*/}
            </Stack>
        </>
    )
}
