import { Backdrop, CircularProgress, Stack, Typography } from "@mui/material"
import { useAppSelector } from "../../store/store"
import theme from "../../theme"

export default function DataMigrationSpinner() {
    const busyTexts = useAppSelector<string[]>(state => state.busy.texts)

    return (
        <Backdrop component={Stack} open sx={{ zIndex: theme.zIndex.modal + 1, backgroundColor: "rgba(255, 255, 255, 0.3)" }}>
            <CircularProgress />
            {busyTexts.map(text => (
                <Typography key={text} color="primary">
                    {text}
                </Typography>
            ))}
        </Backdrop>
    )
}
