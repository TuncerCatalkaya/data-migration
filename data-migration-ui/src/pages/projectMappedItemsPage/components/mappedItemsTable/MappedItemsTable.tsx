import "ag-grid-community/styles/ag-grid.css"
import "ag-grid-community/styles/ag-theme-alpine.css"
import { AgGridReact } from "ag-grid-react"
import { Stack } from "@mui/material"
import { MappedItemResponse, MappingResponse, ScopeResponse } from "../../../../features/projects/projects.types"
import { CellClassParams, ColDef, GetRowIdParams, SelectionChangedEvent, SortChangedEvent, ValueGetterParams } from "ag-grid-community"
import "./MappedItemsTable.css"
import React, { ChangeEvent, Dispatch, SetStateAction, useCallback, useEffect } from "react"
import Pagination from "../../../../components/pagination/Pagination"
import { ProjectsApi } from "../../../../features/projects/projects.api"
import { useParams } from "react-router-dom"
import { ITooltipParams } from "ag-grid-community/dist/types/core/rendering/tooltipComponent"
import { ValueSetterParams } from "ag-grid-community/dist/types/core/entities/colDef"

interface ItemsTableProps {
    rowData: MappedItemResponse[]
    scopeHeaders: string[]
    selectedScope?: ScopeResponse
    selectedMapping?: MappingResponse
    columnDefs: ColDef[]
    setColumnDefs: Dispatch<SetStateAction<ColDef[]>>
    setSelectedItems: Dispatch<SetStateAction<string[]>>
    mapping: string
    fetchMappedItemsData: (mappingId: string, scopeId: string, page: number, pageSize: number, sort?: string) => void
    page: number
    pageSize: number
    totalElements: number
    sort?: string
    onPageChangeHandler: (_: React.MouseEvent<HTMLButtonElement> | null, page: number) => void
    onPageSizeChangeHandler: (e: ChangeEvent<HTMLTextAreaElement | HTMLInputElement>) => void
    onSortChangeHandler: (e: SortChangedEvent) => void
}

export default function MappedItemsTable({
    rowData,
    scopeHeaders,
    selectedScope,
    selectedMapping,
    columnDefs,
    setColumnDefs,
    setSelectedItems,
    mapping,
    fetchMappedItemsData,
    ...itemsTableProps
}: Readonly<ItemsTableProps>) {
    const { projectId } = useParams()

    const [updateMappedItemProperty] = ProjectsApi.useUpdateMappedItemPropertyMutation()

    const getValue = (singleRowData: any, key: string, mappedKey: string) => {
        if (singleRowData.properties?.[mappedKey] != null) {
            return singleRowData.properties[mappedKey].value
        }
        return singleRowData.item.properties[key].value
    }

    useEffect(() => {
        if (selectedScope && selectedMapping && rowData.length > 0) {
            const dynamicColumnDefs: ColDef[] = [
                {
                    headerName: "",
                    field: "checkboxSelection",
                    maxWidth: 50,
                    resizable: false,
                    lockPosition: true,
                    filter: false,
                    editable: false,
                    sortable: false
                },
                ...[...scopeHeaders].flatMap(key =>
                    selectedMapping.mapping[key].map(mappedKey => ({
                        headerName: mappedKey,
                        valueGetter: (params: ValueGetterParams) => getValue(params.data, key, mappedKey),
                        tooltipValueGetter: (params: ITooltipParams) => {
                            if (params.data.properties == null) {
                                return
                            }
                            const originalValue = params.data.properties[mappedKey]?.originalValue
                            if (originalValue === undefined || originalValue === null) {
                                return ""
                            }
                            return `Original value: ${originalValue}`
                        },
                        valueSetter: (params: ValueSetterParams) => {
                            updateMappedItemProperty({
                                projectId: projectId!,
                                mappedItemId: params.data.id,
                                key: mappedKey,
                                newValue: params.newValue ?? ""
                            }).then(response => {
                                if (response) {
                                    fetchMappedItemsData(
                                        selectedScope.id,
                                        selectedMapping.id,
                                        itemsTableProps.page,
                                        itemsTableProps.pageSize,
                                        itemsTableProps.sort
                                    )
                                }
                            })
                            return true
                        },
                        cellStyle: (params: CellClassParams) => {
                            if (params.data.properties == null) {
                                return
                            }
                            const originalValue: string | undefined = params.data.properties[mappedKey]?.originalValue
                            const edited = originalValue !== undefined && originalValue !== null
                            if (edited) {
                                return { background: "#fff3cd", zIndex: -1 }
                            } else {
                                return { background: "inherit", zIndex: -1 }
                            }
                        }
                    }))
                )
            ]
            setColumnDefs(dynamicColumnDefs)
        }
    }, [rowData, setColumnDefs, scopeHeaders, projectId, updateMappedItemProperty, mapping])

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
