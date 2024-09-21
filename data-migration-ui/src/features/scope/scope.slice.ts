import { createSlice, PayloadAction } from "@reduxjs/toolkit"

export type ScopeMap = { [projectId: string]: string }

interface ScopePayload {
    projectId: string
    scope: string
}

interface DelimiterPayload {
    projectId: string
    delimiter: string
}

interface ScopeState {
    scopes: ScopeMap
    delimiters: ScopeMap
}

const initialState: ScopeState = {
    scopes: {},
    delimiters: {}
}

const ScopeSlice = createSlice({
    name: "scopeSlice",
    initialState,
    reducers: {
        addScope: (state, action: PayloadAction<ScopePayload>) => {
            state.scopes[action.payload.projectId] = action.payload.scope
        },
        addDelimiter: (state, action: PayloadAction<DelimiterPayload>) => {
            state.delimiters[action.payload.projectId] = action.payload.delimiter
        }
    }
})

export default ScopeSlice
