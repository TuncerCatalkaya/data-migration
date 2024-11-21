import { SelectedDatabase } from "../hosts/hosts.types"

export interface ProjectResponse {
    id: string
    name: string
    createdBy: string
    createdDate: Date
    lastModifiedDate: Date
}

export interface ScopeResponse {
    id: string
    key: string
    createdDate: Date
}

export type Properties = { [key: string]: ItemPropertiesResponse }
export interface ItemResponse {
    id: string
    properties: Properties
    mappingIds: string[]
}

export interface ItemPropertiesResponse {
    value: string
    originalValue?: string
}

export type Mapping = { [key: string]: string[] }
export interface MappingResponse {
    id: string
    name: string
    createdDate: Date
    finished: boolean
    processing: boolean
    mapping: Mapping
    database: SelectedDatabase
}

export enum ItemStatusResponse {
    MAPPED = "MAPPED",
    MIGRATED = "MIGRATED",
    FAILED = "FAILED"
}
export interface MappedItemResponse {
    id: string
    properties: Properties
    status: ItemStatusResponse
    item: ItemResponse
}

export interface CreateProjectRequest {
    projectName: string
}

export interface ImportDataFileRequest {
    projectId: string
    scopeId: string
    delimiter: string
    file: File
}

export interface ImportDataS3Request {
    scopeId: string
    bucket: string
    key: string
}

export interface InterruptScopeRequest {
    projectId: string
    scopeId: string
}

export interface AddExtraHeaderRequest {
    projectId: string
    scopeId: string
    extraHeader: string
}

export interface ApplyMappingRequest {
    projectId: string
    mappingId: string
    itemIds: string[]
}

export interface ApplyUnmappingRequest {
    projectId: string
    mappedItemIds: string[]
}

export interface UpdateProjectRequest {
    projectId: string
    projectName: string
}

export interface CreateOrGetScopeRequest {
    projectId: string
    scopeKey: string
    external: boolean
}

export interface UpdateItemPropertyRequest {
    projectId: string
    itemId: string
    key: string
    newValue: string
}

export interface UpdateItemPropertiesRequest {
    projectId: string
    itemIds: string[]
    key: string
    newValue: string
}

export interface UpdateMappedItemPropertyRequest {
    projectId: string
    mappedItemId: string
    key: string
    newValue: string
}

export interface CreateOrUpdateMappingsRequest {
    projectId: string
    scopeId: string
    mappingId: string
    databaseId: string
    mappingName: string
    mapping: Mapping
}

export interface IsProjectPermittedRequest {
    projectId: string
}

export interface GetProjectRequest {
    projectId: string
}

export interface GetProjectsRequest {
    page: number
    size: number
    sort?: string
}

export interface GetScopeRequest {
    projectId: string
    scopeId: string
}

export interface GetScopesRequest {
    projectId: string
}

export interface GetItemsRequest {
    projectId: string
    scopeId: string
    mappingId?: string
    filterMappedItems: boolean
    header: string
    search: string
    page: number
    size: number
    sort?: string
}

export interface GetMappingsRequest {
    projectId: string
    scopeId: string
}

export interface GetMappedItemsRequest {
    projectId: string
    mappingId: string
    page: number
    size: number
    sort?: string
}

export interface GetCurrentCheckpointStatusRequest {
    projectId: string
    scopeId: string
}

export interface MarkProjectForDeletionRequest {
    projectId: string
}

export interface MarkScopeForDeletionRequest {
    projectId: string
    scopeId: string
}

export interface MarkMappingForDeletionRequest {
    projectId: string
    mappingId: string
}

export interface RemoveExtraHeaderRequest {
    projectId: string
    scopeId: string
    extraHeader: string
}

export interface GetProjectsResponse {
    content: ProjectResponse[]
    totalElements: number
}

export interface GetScopeHeadersResponse {
    headers: string[]
    extraHeaders: string[]
}

export interface GetItemsResponse {
    content: ItemResponse[]
    totalElements: number
}

export interface GetMappedItemsByMappingResponse {
    content: MappedItemResponse[]
    totalElements: number
}

export interface GetCurrentCheckpointStatusResponse {
    batchesProcessed: number
    totalBatches: number
    processing: boolean
    finished: boolean
    external: boolean
}
