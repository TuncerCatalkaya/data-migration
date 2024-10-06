import { Backdrop, CircularProgress, Stack, Typography } from "@mui/material"
import { useAppSelector } from "../../store/store"
import DataMigrationSpinnerStyles from "./DataMigrationSpinner.styles"

interface DataMigrationSpinnerProps {
    determinate?: {
        text: string
        value: number
    }
}

export default function DataMigrationSpinner({ determinate }: Readonly<DataMigrationSpinnerProps>) {
    const busyTexts = useAppSelector<string[]>(state => state.busy.texts)

    return (
        <Backdrop component={Stack} open sx={DataMigrationSpinnerStyles.backdrop}>
            <Stack alignItems="center" sx={DataMigrationSpinnerStyles.stack}>
                <CircularProgress variant={determinate ? "determinate" : "indeterminate"} value={determinate?.value} />
                {determinate && <Typography color="primary">{determinate.text + `${Math.round(determinate.value)}%`}</Typography>}
                {busyTexts.map(text => (
                    <Typography key={text} color="primary">
                        {text}
                    </Typography>
                ))}
            </Stack>
        </Backdrop>
    )
}
