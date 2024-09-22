export interface ProjectResponse {
    id: string
    name: string
    owner: string
    createdDate: Date
    lastUpdatedDate: Date
}

export interface ScopeResponse {
    id: string
    key: string
    createdDate: Date
}

export interface ItemResponse {
    id: string
    properties: { [key: string]: string }
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
    delimiter: string
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

export interface GetProjectRequest {
    projectId: string
}

export interface GetProjectsRequest {
    page: number
    size: number
    sort?: string
}

export interface GetScopesRequest {
    projectId: string
}

export interface GetItemsRequest {
    projectId: string
    scopeId: string
    page: number
    size: number
    sort?: string
}

export interface GetCurrentCheckpointStatusRequest {
    projectId: string
    scopeId: string
}

export interface DeleteScopeRequest {
    projectId: string
    scopeId: string
}

export interface GetProjectsResponse {
    content: ProjectResponse[]
    totalElements: number
}

export interface ImportDataResponse {
    success: boolean
    skipped: boolean
}

export interface GetItemsResponse {
    content: ItemResponse[]
    totalElements: number
}

export interface GetCurrentCheckpointStatusResponse {
    batchesProcessed: number
    totalBatches: number
    processing: boolean
    finished: boolean
}
