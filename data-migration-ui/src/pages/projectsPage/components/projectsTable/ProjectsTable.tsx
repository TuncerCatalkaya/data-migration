import "ag-grid-community/styles/ag-grid.css"
import "ag-grid-community/styles/ag-theme-alpine.css"
import { AgGridReact } from "ag-grid-react"
import { IconButton, Stack } from "@mui/material"
import "./ProjectsTable.css"
import useNavigate from "../../../../router/hooks/useNavigate"
import { ColDef, ICellRendererParams, SortChangedEvent } from "ag-grid-community"
import { MoreVert } from "@mui/icons-material"
import React, { ChangeEvent, useState } from "react"
import FormatDate from "../../../../utils/FormatDate"
import Pagination from "../../../../components/pagination/Pagination"
import { useTranslation } from "react-i18next"
import { ProjectResponse } from "../../../../features/projects/projects.types"

interface ProjectsTableProps {
    rowData: ProjectResponse[]
    page: number
    pageSize: number
    totalElements: number
    onPageChangeHandler: (_: React.MouseEvent<HTMLButtonElement> | null, page: number) => void
    onPageSizeChangeHandler: (e: ChangeEvent<HTMLTextAreaElement | HTMLInputElement>) => void
    onSortChangeHandler: (e: SortChangedEvent) => void
}

export default function ProjectsTable(projectsTableProps: Readonly<ProjectsTableProps>) {
    const navigate = useNavigate()
    const translation = useTranslation()

    const actionElement = (params: ICellRendererParams) => (
        <IconButton onClick={() => console.log(params.data)}>
            <MoreVert />
        </IconButton>
    )

    const [columnDefs] = useState<ColDef[]>([
        { headerName: translation.t("pages.projects.components.projectsTable.columns.id"), field: "id", tooltipField: "id", maxWidth: 200 },
        { headerName: translation.t("pages.projects.components.projectsTable.columns.projectName"), field: "name", tooltipField: "name" },
        {
            headerName: translation.t("pages.projects.components.projectsTable.columns.createdDate"),
            field: "createdDate",
            maxWidth: 150,
            valueFormatter: params => FormatDate(params.value)
        },
        {
            headerName: translation.t("pages.projects.components.projectsTable.columns.lastUpdatedDate"),
            field: "lastUpdatedDate",
            maxWidth: 150,
            valueFormatter: params => FormatDate(params.value)
        },
        {
            headerName: "",
            field: "action",
            maxWidth: 80,
            sortable: false,
            onCellClicked: () => console.log("open menu"),
            cellRenderer: actionElement
        }
    ])

    const defaultColDef: ColDef = {
        flex: 1,
        resizable: false,
        comparator: () => 0
    }

    const getRowStyle = () => {
        return {
            cursor: "pointer"
        }
    }

    return (
        <Stack>
            <div className="ag-theme-alpine" style={{ width: 900, height: 471, textAlign: "left" }}>
                <AgGridReact
                    rowData={projectsTableProps.rowData}
                    columnDefs={columnDefs}
                    onSortChanged={projectsTableProps.onSortChangeHandler}
                    defaultColDef={defaultColDef}
                    tooltipShowDelay={1000}
                    tooltipInteraction
                    suppressMovableColumns
                    suppressCellFocus
                    suppressColumnMoveAnimation
                    animateRows={false}
                    suppressAnimationFrame
                    getRowStyle={getRowStyle}
                    onCellClicked={e => {
                        if (e.colDef.field === "action") {
                            return
                        }
                        navigate.toProject(e.data.id)
                    }}
                />
            </div>
            <Pagination {...projectsTableProps} />
        </Stack>
    )
}
