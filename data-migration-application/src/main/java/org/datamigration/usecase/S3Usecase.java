package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.model.CompletedPartModel;
import org.datamigration.service.S3Service;
import org.datamigration.usecase.model.GeneratePresignedUrlResponseModel;
import org.datamigration.usecase.model.InitiateMultipartUploadRequestModel;
import org.datamigration.usecase.model.S3ListResponseModel;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class S3Usecase {

    private final S3Service s3Service;

    public InitiateMultipartUploadRequestModel initiateMultipartUpload(String bucket, String key) {
        return InitiateMultipartUploadRequestModel.builder()
                .uploadId(s3Service.initiateMultipartUpload(bucket, key))
                .build();
    }

    public GeneratePresignedUrlResponseModel generatePresignedUrlMultiPartUpload(String bucket, String key, String uploadId, int partNumber) {
        return GeneratePresignedUrlResponseModel.builder()
                .presignedUrl(s3Service.generatePresignedUrlMultiPartUpload(bucket, key, uploadId, partNumber))
                .build();
    }

    public void completeMultipartUpload(String bucket, String key, String uploadId, List<CompletedPartModel> completedParts) {
        s3Service.completeMultipartUpload(bucket, key, uploadId, completedParts);
    }

    public void abortMultipartUpload(String bucket, String key, String uploadId) {
        s3Service.abortMultipartUpload(bucket, key, uploadId);
    }

    public List<S3ListResponseModel> listObjectsV2(String bucket, String projectId) {
        final ListObjectsV2Response listObjectsV2Response = s3Service.listObjectsV2(bucket, projectId);
        return listObjectsV2Response.contents().stream()
                .map(s3Object ->  S3ListResponseModel.builder()
                        .key(s3Object.key())
                        .lastModified(Date.from(s3Object.lastModified()))
                        .size(s3Object.size())
                        .build())
                .toList();
    }

    public void deleteObject(String bucket, String key) {
        s3Service.deleteObject(bucket, key);
    }

}
