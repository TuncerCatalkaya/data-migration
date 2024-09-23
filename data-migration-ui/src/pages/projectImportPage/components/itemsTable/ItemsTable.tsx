import "ag-grid-community/styles/ag-grid.css"
import "ag-grid-community/styles/ag-theme-alpine.css"
import { AgGridReact } from "ag-grid-react"
import { Stack } from "@mui/material"
import "./ItemsTable.css"
import { ItemResponse } from "../../../../features/projects/projects.types"
import { ColDef, SortChangedEvent } from "ag-grid-community"
import React, { ChangeEvent, Dispatch, SetStateAction, useEffect } from "react"
import Pagination from "../../../../components/pagination/Pagination"

interface ItemsTableProps {
    rowData: ItemResponse[]
    columnDefs: ColDef[]
    setColumnDefs: Dispatch<SetStateAction<ColDef[]>>
    page: number
    pageSize: number
    totalElements: number
    onPageChangeHandler: (_: React.MouseEvent<HTMLButtonElement> | null, page: number) => void
    onPageSizeChangeHandler: (e: ChangeEvent<HTMLTextAreaElement | HTMLInputElement>) => void
    onSortChangeHandler: (e: SortChangedEvent) => void
}

export default function ItemsTable({ rowData, columnDefs, setColumnDefs, ...itemsTableProps }: Readonly<ItemsTableProps>) {
    useEffect(() => {
        if (rowData.length > 0) {
            const propertiesKeys = Object.keys(rowData[0].properties)
            const dynamicColumnDefs: ColDef[] = [
                ...propertiesKeys.map(key => ({
                    field: `properties.${key}`,
                    headerName: key,
                    tooltipField: `properties.${key}`
                }))
            ]
            setColumnDefs(dynamicColumnDefs)
        }
    }, [rowData, setColumnDefs])

    const defaultColDef: ColDef = {
        filter: true
    }

    return (
        <Stack>
            <div className="ag-theme-alpine" style={{ height: 500, textAlign: "left" }}>
                <AgGridReact
                    rowData={rowData}
                    columnDefs={columnDefs}
                    defaultColDef={defaultColDef}
                    tooltipShowDelay={1000}
                    tooltipInteraction
                    suppressCellFocus
                />
            </div>
            <Pagination {...itemsTableProps} />
        </Stack>
    )
}
