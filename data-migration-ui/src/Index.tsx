import { createRoot } from "react-dom/client"
import App from "./App"
import { Grid } from "@mui/material"

interface DataMigrationApp {
    init: (containerId: string, language: string, token?: string) => void
    dev: (containerId: string, language: string) => void
}

declare global {
    interface Window {
        DataMigrationApp: DataMigrationApp
    }
}

const init = (containerId: string, element: JSX.Element) => {
    const container = document.getElementById(containerId)
    if (container) {
        const root = createRoot(container)
        root.render(element)
    } else {
        console.error(`Target container ${containerId} is not a DOM element.`)
    }
}

window.DataMigrationApp = {
    init(containerId: string, language: string, token?: string) {
        const element = <App token={token} language={language} />
        init(containerId, element)
    },
    dev(containerId: string, language: string) {
        const element = (
            <Grid container spacing={0} direction="column" alignItems="center" justifyContent="center" sx={{ minHeight: "100vh" }}>
                <Grid item xs={3}>
                    <App language={language} />
                </Grid>
            </Grid>
        )
        init(containerId, element)
    }
}
