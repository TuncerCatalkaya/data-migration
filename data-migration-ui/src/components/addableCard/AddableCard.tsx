import { Box, Button, Paper, Stack, Typography } from "@mui/material"
import { Remove } from "@mui/icons-material"
import { ReactNode } from "react"

interface AddableCardProps {
    label: string
    index: number
    handleClickRemove: () => void
    children: ReactNode
}

export default function AddableCard({ label, index, handleClickRemove, children }: Readonly<AddableCardProps>) {
    return (
        <Paper sx={{ padding: "25px" }}>
            <Stack direction="row">
                <Typography variant="h6">{label + " " + index}</Typography>
                <Box flexGrow={1} />
                {index !== 1 && (
                    <Button variant="contained" color="error" startIcon={<Remove />} onClick={handleClickRemove}>
                        {"Remove " + label}
                    </Button>
                )}
            </Stack>
            {children}
        </Paper>
    )
}
