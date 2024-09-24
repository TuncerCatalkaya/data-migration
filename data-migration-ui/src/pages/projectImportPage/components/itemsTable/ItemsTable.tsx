import "ag-grid-community/styles/ag-grid.css"
import "ag-grid-community/styles/ag-theme-alpine.css"
import { AgGridReact } from "ag-grid-react"
import { Stack } from "@mui/material"
import { ItemResponse } from "../../../../features/projects/projects.types"
import { CellClassParams, ColDef, GetRowIdParams, SortChangedEvent } from "ag-grid-community"
import React, { ChangeEvent, Dispatch, SetStateAction, useEffect } from "react"
import Pagination from "../../../../components/pagination/Pagination"
import { ProjectsApi } from "../../../../features/projects/projects.api"
import { useParams } from "react-router-dom"
import { ValueSetterParams } from "ag-grid-community/dist/types/core/entities/colDef"

interface ItemsTableProps {
    rowData: ItemResponse[]
    scopeHeaders: string[]
    columnDefs: ColDef[]
    setColumnDefs: Dispatch<SetStateAction<ColDef[]>>
    page: number
    pageSize: number
    totalElements: number
    onPageChangeHandler: (_: React.MouseEvent<HTMLButtonElement> | null, page: number) => void
    onPageSizeChangeHandler: (e: ChangeEvent<HTMLTextAreaElement | HTMLInputElement>) => void
    onSortChangeHandler: (e: SortChangedEvent) => void
}

export default function ItemsTable({ rowData, scopeHeaders, columnDefs, setColumnDefs, ...itemsTableProps }: Readonly<ItemsTableProps>) {
    const { projectId } = useParams()

    const [updateItemProperty] = ProjectsApi.useUpdateItemPropertyMutation()

    useEffect(() => {
        if (scopeHeaders && rowData.length > 0) {
            const dynamicColumnDefs: ColDef[] = [
                ...[...scopeHeaders].map(key => ({
                    field: `properties.${key}.value`,
                    headerName: key,
                    valueSetter: (params: ValueSetterParams) => {
                        const newData = {
                            ...params.data,
                            properties: {
                                ...params.data.properties,
                                [key]: {
                                    ...params.data.properties[key],
                                    value: params.newValue,
                                    edited: true
                                }
                            }
                        }
                        updateItemProperty({ projectId: projectId!, itemId: params.data.id, key, value: params.newValue }).then(response => {
                            if (response.data) {
                                params.api.applyTransaction({ update: [newData] })
                            }
                        })
                        return true
                    },
                    cellStyle: (params: CellClassParams) => {
                        if (params.data.properties[key].edited) {
                            return { background: "#fff3cd" }
                        }
                    }
                }))
            ]
            setColumnDefs(dynamicColumnDefs)
        }
    }, [rowData, setColumnDefs])

    const defaultColDef: ColDef = {
        filter: true,
        editable: true
    }

    const getRowId = (params: GetRowIdParams) => params.data.id

    return (
        <Stack>
            <div className="ag-theme-alpine" style={{ height: 488, textAlign: "left" }}>
                <AgGridReact
                    rowData={rowData}
                    columnDefs={columnDefs}
                    defaultColDef={defaultColDef}
                    enableCellTextSelection={true}
                    stopEditingWhenCellsLoseFocus={true}
                    getRowId={getRowId}
                    suppressRowHoverHighlight={true}
                />
            </div>
            <Pagination {...itemsTableProps} />
        </Stack>
    )
}
