import { createRoot } from "react-dom/client"
import App from "./App"

interface DataMigrationApp {
    init: (containerId: string, language: string, token?: string) => void
}

declare global {
    interface Window {
        DataMigrationApp: DataMigrationApp
    }
}

window.DataMigrationApp = {
    init(containerId: string, language: string, token?: string) {
        const element = <App token={token} language={language} />
        const container = document.getElementById(containerId)
        if (container) {
            const root = createRoot(container)
            root.render(element)
        } else {
            console.error(`Target container ${containerId} is not a DOM element.`)
        }
    }
}
