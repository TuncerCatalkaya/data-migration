import { createSlice, PayloadAction } from "@reduxjs/toolkit"

interface MappedItemsScopePayload {
    projectId: string
    scope: string
}

interface MappedItemsMappingPayload {
    projectId: string
    mapping: string
}

interface MappedItemsState {
    scopes: Record<string, string>
    mappings: Record<string, string>
}

const initialState: MappedItemsState = {
    scopes: {},
    mappings: {}
}

const MappedItemsSlice = createSlice({
    name: "mappedItemsSlice",
    initialState,
    reducers: {
        addScope: (state, action: PayloadAction<MappedItemsScopePayload>) => {
            state.scopes[action.payload.projectId] = action.payload.scope
        },
        addMapping: (state, action: PayloadAction<MappedItemsMappingPayload>) => {
            state.mappings[action.payload.projectId] = action.payload.mapping
        }
    }
})

export default MappedItemsSlice
