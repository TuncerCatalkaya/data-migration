import { createRoot } from "react-dom/client"
import App from "./App"

interface DataMigrationApp {
    init: (containerId: string, language: string, token?: string) => void
}

declare global {
    interface Window {
        DataMigrationApp: DataMigrationApp
        dataMigrationBaseUrl: string
        environments: Map<string, string>
    }
}

window.DataMigrationApp = {
    init(containerId: string, language: string, token?: string) {
        const container = document.getElementById(containerId)
        if (container) {
            window.environments = new Map<string, string>()
            const headers = new Headers()
            // headers.set("Authorization", `Bearer ${token}`)
            fetch(`${window.dataMigrationBaseUrl}/data-migration/environments/frontend`, { headers }).then(response =>
                response.json().then(data => {
                    const frontendEnvironments: Record<string, string> = data
                    for (const key in frontendEnvironments) {
                        window.environments.set(key, frontendEnvironments[key])
                    }
                    const root = createRoot(container)
                    root.render(<App token={token} language={language} />)
                })
            )
        } else {
            console.error(`Target container ${containerId} is not a DOM element.`)
        }
    }
}
