/// <reference types="vite/client" />
declare module "*.png"
interface ImportMetaEnv {
    readonly VITE_BASE_URL_ROOT_PATH
    readonly VITE_BASE_URL: string

    readonly VITE_REFRESH_TOKEN_ENDPOINT_BASE_URL: string
    readonly VITE_REFRESH_TOKEN_ENDPOINT_URL: string
    readonly VITE_REFRESH_TOKEN_ENDPOINT_METHOD: string
    readonly VITE_REFRESH_TOKEN_ENDPOINT_ACCESS_TOKEN_NAME: string

    readonly VITE_SNACKBAR_ORIENTATION_VERTICAL: "top" | "bottom"
    readonly VITE_SNACKBAR_ORIENTATION_HORIZONTAL: "right" | "left" | "center"
    readonly VITE_SNACKBAR_OFFSET: string
}
interface ImportMeta {
    readonly env: ImportMetaEnv
}
