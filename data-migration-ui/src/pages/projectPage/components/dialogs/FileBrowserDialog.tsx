import { Alert, AlertColor, Button, Dialog, DialogContent, DialogTitle, Paper, PaperProps, Stack, Typography } from "@mui/material"
import Draggable from "react-draggable"
import { useSnackbar } from "notistack"
import theme from "../../../../theme"
import { useTranslation } from "react-i18next"
import { CloudDownload, CloudUpload, Delete, Storage } from "@mui/icons-material"
import { VisuallyHiddenInput } from "../../../../components/visuallyHiddenInput/VisuallyHiddenInput"
import { ChangeEvent, useCallback, useEffect, useState } from "react"
import TimeStamp from "../../../../utils/TimeStamp"
import DataMigrationSpinner from "../../../../components/dataMigrationSpinner/DataMigrationSpinner"
import FormatDate from "../../../../utils/FormatDate"
import { filesize } from "filesize"
import useConfirmationDialog from "../../../../components/confirmationDialog/hooks/useConfirmationDialog"
import ConfirmationDialog from "../../../../components/confirmationDialog/ConfirmationDialog"
import { S3Api } from "../../../../features/s3/s3.api"
import { CompletedPart, S3ListResponse } from "../../../../features/s3/s3.types"
import pLimit from "p-limit"
import GetFrontendEnvironment from "../../../../utils/GetFrontendEnvironment"

interface FileBrowserDialogProps {
    open: boolean
    handleClickClose: (shouldReload?: boolean) => void
    projectId: string
}

interface AlertType {
    text: string
    severity?: AlertColor
}

function PaperComponent(props: PaperProps) {
    return (
        <Draggable handle="#file-browser-dialog" cancel={'[class*="MuiDialogContent-root"]'}>
            <Paper {...props} />
        </Draggable>
    )
}

const MB = 1024 * 1024
const bucket = "data-migration"

export default function FileBrowserDialog({ open, handleClickClose, projectId }: Readonly<FileBrowserDialogProps>) {
    const [uploadProgress, setUploadProgress] = useState<number>(0)
    const [isUploading, setIsUploading] = useState<boolean>(false)
    const [fileBrowserObjects, setFileBrowserObjects] = useState<S3ListResponse[]>([])
    const [alert, setAlert] = useState<AlertType>({ text: "" })
    const [fileBrowserObjectToDelete, setFileBrowserObjectToDelete] = useState<string>("")
    const { openConfirmationDialog, handleClickCloseConfirmationDialog, handleClickOpenConfirmationDialog } = useConfirmationDialog()

    const [initiateMultipartUpload] = S3Api.useInitiateMultipartUploadMutation()
    const [generatePresignedUrlMultiPartUpload] = S3Api.useGeneratePresignedUrlMultiPartUploadMutation()
    const [completeMultipartUpload] = S3Api.useCompleteMultipartUploadMutation()
    const [listObjectsV2] = S3Api.useListObjectsV2Mutation()
    const [deleteObject] = S3Api.useDeleteObjectMutation()

    const { enqueueSnackbar } = useSnackbar()
    const translation = useTranslation()

    const closeDialog = (shouldReload = false) => handleClickClose(shouldReload)

    const handleCloseAlert = () => setAlert({ text: "" })

    const handleFileChange = async (e: ChangeEvent<HTMLInputElement>) => {
        const files = e.target.files
        if (files) {
            const file = files[0]
            if (!file.name.toLowerCase().endsWith(".csv")) {
                enqueueSnackbar("Please upload a CSV file.", { variant: "error" })
            } else if (isUploading) {
                enqueueSnackbar("Another file is already uploading.", { variant: "warning" })
            } else {
                try {
                    const timeStamp = TimeStamp()
                    const splittedFileName = file.name.split(".")
                    const fileName = splittedFileName[0]
                    const extension = splittedFileName[1]
                    const key = projectId + "/" + fileName + "-" + timeStamp + "." + extension

                    const CHUNK_SIZE = GetFrontendEnvironment("VITE_S3_CHUNK_SIZE_IN_MB") * MB
                    const CONCURRENCY = +GetFrontendEnvironment("VITE_S3_CONCURRENCY")

                    setIsUploading(true)

                    const initiateMultipartUploadResponse = await initiateMultipartUpload({ bucket, key }).unwrap()

                    const uploadId = initiateMultipartUploadResponse.uploadId
                    const totalParts = Math.ceil(file.size / CHUNK_SIZE)

                    const uploadProgress = (1 / totalParts) * 100

                    const promises: Promise<CompletedPart>[] = []

                    const limit = pLimit(CONCURRENCY)
                    for (let partNumber = 1; partNumber <= totalParts; partNumber++) {
                        const start = (partNumber - 1) * CHUNK_SIZE
                        const end = Math.min(start + CHUNK_SIZE, file.size)
                        const filePart = file.slice(start, end)

                        const uploadPromise = limit(() => uploadFilePart(filePart, bucket, key, uploadId, partNumber, uploadProgress))

                        promises.push(uploadPromise)
                    }

                    const uploadedParts = await Promise.all(promises)

                    const completeMultipartUploadResponse = await completeMultipartUpload({ bucket, key, uploadId, completedParts: uploadedParts })
                    setIsUploading(false)
                    setUploadProgress(0)
                    if (completeMultipartUploadResponse.error) {
                        setAlert({ text: "Could not upload file", severity: "error" })
                    } else {
                        setAlert({ text: "File is uploaded", severity: "success" })
                        await fetchFileBrowserObjects()
                    }
                } catch (e) {
                    setAlert({ text: "Something went wrong during the upload", severity: "error" })
                    setIsUploading(false)
                    setUploadProgress(0)
                    console.error(e)
                }
            }
        }
        e.target.value = ""
    }

    const uploadFilePart = async (
        filePart: Blob,
        bucket: string,
        key: string,
        uploadId: string,
        partNumber: number,
        uploadProgress: number
    ): Promise<CompletedPart> => {
        try {
            const generatePresignedUrlMultiPartUploadResponse = await generatePresignedUrlMultiPartUpload({ bucket, key, uploadId, partNumber }).unwrap()

            const response = await fetch(generatePresignedUrlMultiPartUploadResponse.presignedUrl, {
                method: "PUT",
                body: filePart
            })

            if (!response.ok) {
                throw new Error(`Failed to upload part ${partNumber}`)
            }

            const etag = response.headers.get("Etag")
            if (!etag) throw new Error("Missing ETag in the response")

            setUploadProgress(prevState => prevState + uploadProgress)

            return { etag, partNumber }
        } catch (error) {
            console.error(`Error uploading part ${partNumber}:`, error)
            throw error
        }
    }

    const handleClickDeleteFileBrowserObject = async (key: string) => {
        const deleteObjectResponse = await deleteObject({ bucket, key })
        if (deleteObjectResponse.error) {
            setAlert({ text: "Could not delete file", severity: "error" })
        } else {
            setAlert({ text: "File deleted", severity: "success" })
            setFileBrowserObjectToDelete("")
            await fetchFileBrowserObjects()
        }
    }

    const fetchFileBrowserObjects = useCallback(async () => {
        const listObjectsV2Response = await listObjectsV2({ bucket, projectId })
        if (listObjectsV2Response.data) {
            setFileBrowserObjects(listObjectsV2Response.data)
        } else if (listObjectsV2Response.error) {
            setAlert({ text: "Could not retrieve files", severity: "error" })
        }
    }, [listObjectsV2, projectId])

    useEffect(() => {
        fetchFileBrowserObjects()
    }, [fetchFileBrowserObjects])

    return (
        <>
            {isUploading && <DataMigrationSpinner determinate={{ text: "Uploading: ", value: uploadProgress }} />}
            {openConfirmationDialog && (
                <ConfirmationDialog
                    open={openConfirmationDialog}
                    handleClickClose={handleClickCloseConfirmationDialog}
                    handleClickYes={() => handleClickDeleteFileBrowserObject(fileBrowserObjectToDelete)}
                >
                    <Stack spacing={2}>
                        <Typography variant="h6">Are you sure you want to delete the file?</Typography>
                        <Typography sx={{ fontWeight: "bold" }}>{fileBrowserObjectToDelete}</Typography>
                    </Stack>
                </ConfirmationDialog>
            )}
            <Dialog
                open={open}
                onClose={() => closeDialog()}
                aria-labelledby="file-browser-dialog"
                PaperComponent={PaperComponent}
                sx={{ zIndex: theme.zIndex.modal }}
            >
                <DialogTitle sx={{ cursor: "move" }}>
                    <Stack spacing={1}>
                        <Stack direction="row" display="flex" justifyContent="space-between">
                            <Stack direction="row" alignItems="center" spacing={1}>
                                <Storage />
                                <Typography variant="h6">{translation.t("pages.project.components.dialogs.fileBrowserDialog.title")}</Typography>
                            </Stack>
                            <Button
                                color="secondary"
                                component="label"
                                role={undefined}
                                variant="contained"
                                tabIndex={-1}
                                startIcon={<CloudUpload />}
                                sx={{ marginRight: 2 }}
                            >
                                Upload
                                <VisuallyHiddenInput type="file" accept=".csv" onChange={handleFileChange} />
                            </Button>
                        </Stack>
                        {alert.text && (
                            <Alert severity={alert.severity} onClose={handleCloseAlert}>
                                {alert.text}
                            </Alert>
                        )}
                    </Stack>
                </DialogTitle>
                <DialogContent>
                    <Stack spacing={1} padding={2}>
                        {fileBrowserObjects.length === 0 && <Typography variant="h6">No files uploaded yet for this project</Typography>}
                        {[...fileBrowserObjects]
                            .sort((a, b) => new Date(b.lastModified).getTime() - new Date(a.lastModified).getTime())
                            .map(fileBrowserObject => (
                                <Paper key={fileBrowserObject.key} elevation={5} sx={{ minWidth: 500, padding: 2, borderRadius: 5 }}>
                                    <Stack spacing={1}>
                                        <Typography
                                            noWrap
                                            sx={{ maxWidth: 500, textOverflow: "ellipsis", overflow: "hidden", whiteSpace: "nowrap", fontWeight: "bold" }}
                                        >
                                            {fileBrowserObject.key.split("/")[1]}
                                        </Typography>
                                        <Typography>{filesize(fileBrowserObject.size, { standard: "iec" })}</Typography>
                                        <Stack direction="row" display="flex" justifyContent="space-between" alignItems="center" spacing={1}>
                                            <Typography sx={{ fontStyle: "italic" }}>{FormatDate(fileBrowserObject.lastModified)}</Typography>
                                            <Stack direction="row" spacing={1}>
                                                <Button
                                                    color="error"
                                                    variant="contained"
                                                    endIcon={<Delete />}
                                                    onClick={() => {
                                                        setFileBrowserObjectToDelete(fileBrowserObject.key)
                                                        handleClickOpenConfirmationDialog()
                                                    }}
                                                >
                                                    Delete
                                                </Button>
                                                <Button variant="contained" endIcon={<CloudDownload />}>
                                                    Start Import
                                                </Button>
                                            </Stack>
                                        </Stack>
                                    </Stack>
                                </Paper>
                            ))}
                    </Stack>
                </DialogContent>
            </Dialog>
        </>
    )
}
