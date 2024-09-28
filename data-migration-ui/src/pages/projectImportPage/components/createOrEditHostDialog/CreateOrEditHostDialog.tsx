import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Paper, PaperProps, Stack, TextField, Typography } from "@mui/material"
import Draggable from "react-draggable"
import theme from "../../../../theme"
import { useTranslation } from "react-i18next"
import { Add, Dns } from "@mui/icons-material"
import { ChangeEvent, useEffect, useState } from "react"
import AddableCard from "../../../../components/addableCard/AddableCard"
import { InputField } from "../../../../components/addableCard/AddableCard.types"
import { v4 as uuidv4 } from "uuid"
import { HostsApi } from "../../../../features/hosts/hosts.api"
import { Host } from "../../../../features/hosts/hosts.types"

interface FileBrowserDialogProps {
    open: boolean
    handleClickClose: (shouldReload?: boolean) => void
    hostToEdit?: Host
}

function PaperComponent(props: PaperProps) {
    return (
        <Draggable handle="#create-or-edit-mapping-dialog" cancel={'[class*="MuiDialogContent-root"]'}>
            <Paper {...props} />
        </Draggable>
    )
}

export default function CreateOrEditHostDialog({ open, handleClickClose, hostToEdit }: Readonly<FileBrowserDialogProps>) {
    const [hostName, setHostName] = useState("")
    const [hostUrl, setHostUrl] = useState("")
    const [databases, setDatabases] = useState<InputField[]>([
        {
            id: uuidv4(),
            dbId: "",
            value: ""
        }
    ])

    const [createOrUpdateHost] = HostsApi.useCreateOrUpdateHostMutation()

    const translation = useTranslation()

    const handleHostNameChange = async (event: ChangeEvent<HTMLInputElement>) => {
        const newHostName = event.target.value
        setHostName(newHostName)
    }
    const handleHostUrlChange = async (event: ChangeEvent<HTMLInputElement>) => {
        const newHostUrl = event.target.value
        setHostUrl(newHostUrl)
    }
    const handleDatabaseChange = (id: string, value: string): void => {
        const updatedDatabases = databases.map(database => {
            if (database.id === id) {
                return { ...database, value }
            }
            return database
        })
        setDatabases(updatedDatabases)
    }

    const addDatabase = (): void => setDatabases([...databases, { id: uuidv4(), dbId: "", value: "" }])
    const removeDatabase = (id: string): void => setDatabases(databases.filter(field => field.id !== id))

    const handleClickSubmit = async () => {
        const host: Host = {
            id: hostToEdit?.id ?? "",
            name: hostName,
            url: hostUrl,
            databases: databases.map(database => {
                return {
                    id: database.dbId,
                    name: database.value
                }
            })
        }
        const createOrUpdateHostResponse = await createOrUpdateHost(host)
        if (createOrUpdateHostResponse.data) {
            handleClickClose(true)
        }
    }

    useEffect(() => {
        if (hostToEdit) {
            setHostName(hostToEdit.name)
            setHostUrl(hostToEdit.url)
            setDatabases(
                hostToEdit.databases.map(database => ({
                    id: database.id,
                    dbId: database.id,
                    value: database.name
                }))
            )
        }
    }, [hostToEdit, open])

    return (
        <Dialog
            open={open}
            onClose={() => handleClickClose()}
            aria-labelledby="create-or-edit-mapping-dialog"
            PaperComponent={PaperComponent}
            PaperProps={{
                style: {
                    bottom: "10%"
                }
            }}
            sx={{ zIndex: theme.zIndex.modal }}
        >
            <DialogTitle sx={{ cursor: "move" }}>
                <Stack spacing={1}>
                    <Stack direction="row" display="flex" justifyContent="space-between">
                        <Stack direction="row" alignItems="center" spacing={1}>
                            <Dns />
                            {!hostToEdit && (
                                <Typography variant="h6">
                                    {translation.t("pages.projectImport.components.dialogs.createOrEditHostDialog.create.title")}
                                </Typography>
                            )}
                            {hostToEdit && (
                                <Typography variant="h6">
                                    {translation.t("pages.projectImport.components.dialogs.createOrEditHostDialog.edit.title")}
                                </Typography>
                            )}
                        </Stack>
                    </Stack>
                </Stack>
            </DialogTitle>
            <DialogContent>
                <Stack spacing={2}>
                    <Paper sx={{ padding: "25px" }}>
                        <Typography variant="h6">Host</Typography>
                        <Stack direction="row" alignItems="center" spacing={2} sx={{ padding: "10px" }}>
                            <TextField required label="Name" value={hostName} onChange={handleHostNameChange} />
                            <TextField required label="URL" value={hostUrl} onChange={handleHostUrlChange} />
                        </Stack>
                    </Paper>
                    {databases.map((database, index) => (
                        <AddableCard key={database.id} label={"Database"} index={index + 1} handleClickRemove={() => removeDatabase(database.id)}>
                            <Stack direction="row" alignItems="center" spacing={2} sx={{ padding: "10px" }}>
                                <TextField required label="Name" value={database.value} onChange={e => handleDatabaseChange(database.id, e.target.value)} />
                            </Stack>
                        </AddableCard>
                    ))}
                    <Button variant="contained" startIcon={<Add />} onClick={addDatabase}>
                        Add database
                    </Button>
                </Stack>
            </DialogContent>
            <DialogActions>
                <Button variant="contained" onClick={handleClickSubmit}>
                    Submit
                </Button>
                <Button variant="contained" color="error" onClick={() => handleClickClose()}>
                    Cancel
                </Button>
            </DialogActions>
        </Dialog>
    )
}