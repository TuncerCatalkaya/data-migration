import { Outlet } from "react-router-dom"
import ProjectTabs from "./components/projectTabs/ProjectTabs"
import { Box, Stack, Typography } from "@mui/material"
import { NavigateBefore } from "@mui/icons-material"
import useNavigate from "../../router/hooks/useNavigate"
import ConfirmationDialog from "../../components/confirmationDialog/ConfirmationDialog"
import useConfirmationDialog from "../../components/confirmationDialog/hooks/useConfirmationDialog"

export default function ProjectPage() {
    const { toProjects } = useNavigate()
    const { openConfirmationDialog, handleClickCloseConfirmationDialog, handleClickOpenConfirmationDialog } = useConfirmationDialog()
    return (
        <>
            {openConfirmationDialog && (
                <ConfirmationDialog open={openConfirmationDialog} handleClickClose={handleClickCloseConfirmationDialog} handleClickYes={() => toProjects()}>
                    <Stack spacing={2}>
                        <Typography variant="body1">Are you sure you want to go back to the project overview?</Typography>
                    </Stack>
                </ConfirmationDialog>
            )}
            <Stack spacing={2}>
                <NavigateBefore onClick={handleClickOpenConfirmationDialog} sx={{ ":hover": { cursor: "pointer" } }} />
                <Box>
                    <ProjectTabs />
                    <Box
                        alignItems="center"
                        sx={{
                            backgroundColor: "#f5f5f5",
                            padding: "20px",
                            borderRadius: "0 0 8px 8px",
                            width: "90vw",
                            height: "50vh",
                            margin: "0 auto",
                            display: "flex",
                            justifyContent: "center"
                        }}
                    >
                        <Outlet />
                    </Box>
                </Box>
            </Stack>
        </>
    )
}
