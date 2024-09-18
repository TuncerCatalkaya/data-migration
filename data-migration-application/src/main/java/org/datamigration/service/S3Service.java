package org.datamigration.service;

import lombok.RequiredArgsConstructor;
import org.datamigration.model.CompletedPartModel;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public String initiateMultipartUpload(String bucket, String key) {
        final CreateMultipartUploadRequest createMultipartUploadPresignRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        final CreateMultipartUploadResponse createMultipartUploadResponse =
                s3Client.createMultipartUpload(createMultipartUploadPresignRequest);

        return createMultipartUploadResponse.uploadId();
    }

    public String generatePresignedUrlMultiPartUpload(String bucket, String key, String uploadId, int partNumber) {
        final UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                .uploadPartRequest(uploadPartRequestBuilder -> uploadPartRequestBuilder
                        .bucket(bucket)
                        .key(key)
                        .uploadId(uploadId)
                        .partNumber(partNumber))
                .signatureDuration(Duration.ofHours(1))
                .build();

        PresignedUploadPartRequest presignedRequest = s3Presigner.presignUploadPart(presignRequest);
        return presignedRequest.url().toString();
    }

    public void completeMultipartUpload(String bucket, String key, String uploadId, long lineCount, List<CompletedPartModel> completedParts) {
        final CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(multiPartUploadBuilder -> multiPartUploadBuilder
                        .parts(completedParts.stream()
                                .map(completedPart -> CompletedPart.builder()
                                        .eTag(completedPart.getETag())
                                        .partNumber(completedPart.getPartNumber())
                                        .build())
                                .toList()
                        )
                )
                .build();

        s3Client.completeMultipartUpload(completeMultipartUploadRequest);

        final PutObjectTaggingRequest putObjectTaggingRequest = PutObjectTaggingRequest.builder()
                .bucket(bucket)
                .key(key)
                .tagging(tagginBuilder -> tagginBuilder
                        .tagSet(Tag.builder()
                                .key("lineCount")
                                .value(String.valueOf(lineCount))
                                .build()
                        )
                )
                .build();
        s3Client.putObjectTagging(putObjectTaggingRequest);
    }

    public void abortMultipartUpload(String bucket,String key, String uploadId) {
        final AbortMultipartUploadRequest abortMultipartUploadRequest = AbortMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .build();
        s3Client.abortMultipartUpload(abortMultipartUploadRequest);
    }

    public ResponseInputStream<GetObjectResponse> getS3Object(String bucket, String key) {
        final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObject(getObjectRequest);
    }

    public GetObjectTaggingResponse getS3ObjectTags(String bucket, String key) {
        final GetObjectTaggingRequest getObjectTaggingRequest = GetObjectTaggingRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObjectTagging(getObjectTaggingRequest);
    }

    public ListObjectsV2Response listObjectsV2(String bucket, String projectId) {
        final ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(projectId + "/")
                .build();
        return s3Client.listObjectsV2(listObjectsV2Request);
    }

    public void deleteObject(String bucket, String key) {
        final DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }
}
