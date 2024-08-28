/// <reference types="vite/client" />
declare module "*.png"
interface ImportMetaEnv {
    readonly VITE_BASE_URL: string
    readonly VITE_AUTH_BASE_URL: string
    readonly VITE_REFRESH_TOKEN_ENDPOINT_BASE_URL: string
    readonly VITE_REFRESH_TOKEN_ENDPOINT_URL: string
    readonly VITE_REFRESH_TOKEN_ENDPOINT_METHOD: string
    readonly VITE_REFRESH_TOKEN_ENDPOINT_ACCESS_TOKEN_NAME: string
}
interface ImportMeta {
    readonly env: ImportMetaEnv
}
