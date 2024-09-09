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

export interface GetProjectsRequest {
    page: number
    size: number
    sort?: string
}

export interface GetProjectsResponse {
    content: ProjectInformationResponse[]
    totalElements: number
}
