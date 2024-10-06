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

export interface SelectedDatabase {
    id: string
    name: string
    host: SelectedHost
}

export interface SelectedHost {
    id: string
    name: string
    url: string
}

export interface DeleteHostRequest {
    hostId: string
}
