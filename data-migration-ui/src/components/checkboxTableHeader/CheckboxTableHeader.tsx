import { IHeaderParams } from "ag-grid-community"
import { Checkbox } from "@mui/material"
import { useEffect, useState } from "react"
import { ItemResponse } from "../../features/projects/projects.types"

interface CheckboxTableHeaderProps extends IHeaderParams {
    mapping: string
    rowData: ItemResponse[]
}

export default function CheckboxTableHeader(checkboxTableHeaderProps: Readonly<CheckboxTableHeaderProps>) {
    const { rowData, mapping } = checkboxTableHeaderProps
    const [checked, setChecked] = useState(false)

    const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>) => {
        const isChecked = event.target.checked
        setChecked(isChecked)

        checkboxTableHeaderProps.api.forEachNode(node => {
            if (mapping !== "select" && !node.data.mappingIds.includes(mapping)) {
                node.setSelected(isChecked)
            }
        })
    }

    useEffect(() => {
        setChecked(false)
        checkboxTableHeaderProps.api.forEachNode(node => node.setSelected(false))
    }, [rowData, mapping])

    return <Checkbox color="info" disableRipple checked={checked} onChange={handleSelectAll} sx={{ minWidth: 10 }} />
}
