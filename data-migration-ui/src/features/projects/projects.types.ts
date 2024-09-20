export interface CreateProjectRequest {
    projectName: string
}

export interface ProjectInformationResponse {
    id: string
    name: string
    owner: string
    createdDate: Date
    lastUpdatedDate: Date
}

export interface ImportDataFileRequest {
    projectId: string
    file: File
}

export interface ImportDataS3Request {
    bucket: string
    key: string
}

export interface ImportDataResponse {
    success: boolean
    skipped: boolean
}

export interface GetProjectRequest {
    projectId: string
}

export interface GetProjectsRequest {
    page: number
    size: number
    sort?: string
}

export interface GetProjectsResponse {
    content: ProjectInformationResponse[]
    totalElements: number
}

export interface GetCurrentCheckpointStatusRequest {
    projectId: string
    scopeId: string
}

export interface GetCurrentCheckpointStatusResponse {
    batchesProcessed: number
    totalBatches: number
}
