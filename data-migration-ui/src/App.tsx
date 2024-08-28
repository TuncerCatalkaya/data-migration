import { Suspense } from "react"
import { CssBaseline, ThemeProvider } from "@mui/material"
import theme from "./theme"
import { SnackbarProvider } from "notistack"
import { store } from "./store/store"
import { Provider } from "react-redux"
import AppLoader from "./AppLoader"
import i18next from "i18next"
import HttpApi from "i18next-http-backend"
import { initReactI18next } from "react-i18next"
import Languages from "./constants/Languages"

interface AppProps {
    token?: string
    language: string
}

function App(appProps: Readonly<AppProps>) {
    return (
        <Suspense>
            <ThemeProvider theme={theme}>
                <CssBaseline />
                <SnackbarProvider
                    anchorOrigin={{ vertical: "top", horizontal: "right" }}
                    autoHideDuration={6000}
                    style={{ whiteSpace: "pre-wrap", marginTop: "100px" }}
                >
                    <Provider store={store}>
                        <AppLoader {...appProps} />
                    </Provider>
                </SnackbarProvider>
            </ThemeProvider>
        </Suspense>
    )
}

i18next
    .use(HttpApi)
    .use(initReactI18next)
    .init({
        backend: {
            loadPath: import.meta.env.VITE_BASE_URL + "/locales/{{lng}}/{{ns}}.json"
        },
        lng: "en",
        fallbackLng: Languages.en.language,
        supportedLngs: Object.values(Languages).map(language => language.language),
        debug: import.meta.env.DEV,
        interpolation: {
            escapeValue: false
        }
    })

export default App
