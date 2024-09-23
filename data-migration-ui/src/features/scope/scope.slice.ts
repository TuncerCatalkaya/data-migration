import { createSlice, PayloadAction } from "@reduxjs/toolkit"

export type ScopeMap = { [projectId: string]: string }

interface ScopePayload {
    projectId: string
    scope: string
}

interface ScopeState {
    scopes: ScopeMap
}

const initialState: ScopeState = {
    scopes: {}
}

const ScopeSlice = createSlice({
    name: "scopeSlice",
    initialState,
    reducers: {
        addScope: (state, action: PayloadAction<ScopePayload>) => {
            state.scopes[action.payload.projectId] = action.payload.scope
        }
    }
})

export default ScopeSlice
