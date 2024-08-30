export interface CreateProjectRequest {
    projectName: string
}

export interface CreateProjectResponse {
    id: string
    name: string
    owner: number
    createdDate: Date
    lastUpdatedDate: Date
}
