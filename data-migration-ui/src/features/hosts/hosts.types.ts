export interface Host {
    id: string
    name: string
    url: string
    databases: Database[]
}

export interface Database {
    id: string
    name: string
}
