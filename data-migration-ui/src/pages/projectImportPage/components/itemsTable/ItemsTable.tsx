import "ag-grid-community/styles/ag-grid.css"
import "ag-grid-community/styles/ag-theme-alpine.css"
import { AgGridReact } from "ag-grid-react"
import { Stack } from "@mui/material"
import { ItemResponse } from "../../../../features/projects/projects.types"
import { CellClassParams, CheckboxSelectionCallbackParams, ColDef, GetRowIdParams, SelectionChangedEvent, SortChangedEvent } from "ag-grid-community"
import "./ItemsTable.css"
import React, { ChangeEvent, Dispatch, SetStateAction, useCallback, useEffect } from "react"
import Pagination from "../../../../components/pagination/Pagination"
import { ProjectsApi } from "../../../../features/projects/projects.api"
import { useParams } from "react-router-dom"
import { ValueSetterParams } from "ag-grid-community/dist/types/core/entities/colDef"
import { ITooltipParams } from "ag-grid-community/dist/types/core/rendering/tooltipComponent"
import CheckboxTableHeader from "../../../../components/checkboxTableHeader/CheckboxTableHeader"

interface ItemsTableProps {
    rowData: ItemResponse[]
    scopeHeaders: string[]
    columnDefs: ColDef[]
    setColumnDefs: Dispatch<SetStateAction<ColDef[]>>
    setSelectedItems: Dispatch<SetStateAction<string[]>>
    mapping: string
    page: number
    pageSize: number
    totalElements: number
    onPageChangeHandler: (_: React.MouseEvent<HTMLButtonElement> | null, page: number) => void
    onPageSizeChangeHandler: (e: ChangeEvent<HTMLTextAreaElement | HTMLInputElement>) => void
    onSortChangeHandler: (e: SortChangedEvent) => void
}

export default function ItemsTable({
    rowData,
    scopeHeaders,
    columnDefs,
    setColumnDefs,
    setSelectedItems,
    mapping,
    ...itemsTableProps
}: Readonly<ItemsTableProps>) {
    const { projectId } = useParams()

    const [updateItemProperty] = ProjectsApi.useUpdateItemPropertyMutation()

    useEffect(() => {
        if (scopeHeaders && rowData.length > 0) {
            const dynamicColumnDefs: ColDef[] = [
                {
                    headerName: "",
                    field: "checkboxSelection",
                    maxWidth: 50,
                    resizable: false,
                    headerComponent: rowData && CheckboxTableHeader,
                    headerComponentParams: {
                        mapping,
                        rowData
                    },
                    checkboxSelection: (params: CheckboxSelectionCallbackParams) => {
                        return !params.data.mappingIds.includes(mapping)
                    },
                    lockPosition: true,
                    filter: false,
                    editable: false,
                    sortable: false
                },
                ...[...scopeHeaders].map(key => ({
                    headerName: key,
                    field: `properties.${key}.value`,
                    tooltipValueGetter: (params: ITooltipParams) => {
                        const originalValue = params.data.properties[key]?.originalValue
                        if (originalValue === undefined || originalValue === null) {
                            return ""
                        }
                        return `Original value: ${originalValue}`
                    },
                    valueSetter: (params: ValueSetterParams) => {
                        updateItemProperty({ projectId: projectId!, itemId: params.data.id, key, newValue: params.newValue ?? "" }).then(response => {
                            if (response.data) {
                                const newData = {
                                    ...params.data,
                                    properties: {
                                        ...params.data.properties,
                                        [key]: {
                                            ...params.data.properties[key],
                                            value: response.data.properties[key].value,
                                            originalValue: response.data.properties[key].originalValue
                                        }
                                    }
                                }
                                params.api.applyTransaction({ update: [newData] })
                            }
                        })
                        return true
                    },
                    cellStyle: (params: CellClassParams) => {
                        const originalValue: string | undefined = params.data.properties[key]?.originalValue
                        const edited = originalValue !== undefined && originalValue !== null
                        if (edited) {
                            return { background: "#fff3cd", zIndex: -1 }
                        } else {
                            return { background: "inherit", zIndex: -1 }
                        }
                    }
                }))
            ]
            setColumnDefs(dynamicColumnDefs)
        }
    }, [rowData, setColumnDefs, scopeHeaders, projectId, updateItemProperty, mapping])

    const defaultColDef: ColDef = {
        filter: true,
        editable: true
    }

    const getRowId = (params: GetRowIdParams) => params.data.id

    const onSelectionChanged = useCallback((e: SelectionChangedEvent) => {
        const selectedNodes = e.api.getSelectedNodes()
        const itemIds = selectedNodes.map(node => node.id!)
        setSelectedItems(itemIds)
    }, [])

    return (
        <Stack>
            <div className="ag-theme-alpine" style={{ height: 488, textAlign: "left" }}>
                <AgGridReact
                    rowData={rowData}
                    columnDefs={columnDefs}
                    defaultColDef={defaultColDef}
                    tooltipShowDelay={1000}
                    tooltipInteraction
                    enableCellTextSelection={true}
                    stopEditingWhenCellsLoseFocus={true}
                    getRowId={getRowId}
                    rowSelection="multiple"
                    suppressRowHoverHighlight={true}
                    suppressRowClickSelection={true}
                    suppressDragLeaveHidesColumns={true}
                    onSelectionChanged={onSelectionChanged}
                />
            </div>
            <Pagination {...itemsTableProps} />
        </Stack>
    )
}
