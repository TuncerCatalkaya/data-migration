import { createSlice, PayloadAction } from "@reduxjs/toolkit"

interface MappedItemsScopePayload {
    projectId: string
    scope: string
}
interface MappedItemsMappingPayload {
    projectId: string
    mapping: string
}
interface MappedItemsFilterMigratedItemPayload {
    projectId: string
    filterMigratedItem: boolean
}

interface MappedItemsState {
    scopes: Record<string, string>
    mappings: Record<string, string>
    filterMigratedItems: Record<string, boolean>
}
const initialState: MappedItemsState = {
    scopes: {},
    mappings: {},
    filterMigratedItems: {}
}

const MappedItemsSlice = createSlice({
    name: "mappedItemsSlice",
    initialState,
    reducers: {
        putScope: (state, action: PayloadAction<MappedItemsScopePayload>) => {
            state.scopes[action.payload.projectId] = action.payload.scope
        },
        putMapping: (state, action: PayloadAction<MappedItemsMappingPayload>) => {
            state.mappings[action.payload.projectId] = action.payload.mapping
        },
        putFilterMigratedItem: (state, action: PayloadAction<MappedItemsFilterMigratedItemPayload>) => {
            state.filterMigratedItems[action.payload.projectId] = action.payload.filterMigratedItem
        }
    }
})

export default MappedItemsSlice
