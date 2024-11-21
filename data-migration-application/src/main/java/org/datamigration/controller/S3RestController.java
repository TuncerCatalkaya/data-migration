package org.datamigration.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.datamigration.model.CompletedPartModel;
import org.datamigration.usecase.S3Usecase;
import org.datamigration.usecase.model.GeneratePresignedUrlResponseModel;
import org.datamigration.usecase.model.InitiateMultipartUploadRequestModel;
import org.datamigration.usecase.model.S3ListResponseModel;
import org.datamigration.utils.DataMigrationUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "/s3")
@RestController
@RequestMapping("${server.root.path}/s3")
@RequiredArgsConstructor
public class S3RestController {

    private final S3Usecase s3Usecase;

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping("/multipart-upload/initiate")
    public InitiateMultipartUploadRequestModel initiateMultipartUpload(@AuthenticationPrincipal Jwt jwt,
                                                                       @RequestParam String bucket, @RequestParam String key) {
        return s3Usecase.initiateMultipartUpload(bucket, key, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping("/multipart-upload/complete")
    public void completeMultipartUpload(@AuthenticationPrincipal Jwt jwt, @RequestParam String bucket, @RequestParam String key,
                                        @RequestParam String uploadId, @RequestParam long lineCount,
                                        @RequestParam String delimiter, @RequestBody List<CompletedPartModel> completedParts) {
        s3Usecase.completeMultipartUpload(bucket, key, uploadId, lineCount,
                DataMigrationUtils.delimiterStringToCharMapper(delimiter), completedParts, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping("/multipart-upload/abort")
    public void abortMultipartUpload(@AuthenticationPrincipal Jwt jwt, @RequestParam String bucket, @RequestParam String key,
                                     @RequestParam String uploadId) {
        s3Usecase.abortMultipartUpload(bucket, key, uploadId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/multipart-upload/presigned-url")
    public GeneratePresignedUrlResponseModel generatePresignedUrlMultiPartUpload(@AuthenticationPrincipal Jwt jwt,
                                                                                 @RequestParam String bucket,
                                                                                 @RequestParam String key,
                                                                                 @RequestParam String uploadId,
                                                                                 @RequestParam int partNumber) {
        return s3Usecase.generatePresignedUrlMultiPartUpload(bucket, key, uploadId, partNumber,
                DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/objects")
    public List<S3ListResponseModel> listObjectsV2(@AuthenticationPrincipal Jwt jwt, @RequestParam String bucket,
                                                   @RequestParam String projectId) {
        return s3Usecase.listObjectsV2(bucket, projectId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @DeleteMapping("/objects")
    public void deleteObject(@AuthenticationPrincipal Jwt jwt, @RequestParam String bucket, @RequestParam String key) {
        s3Usecase.deleteObject(bucket, key, DataMigrationUtils.getJwtUserId(jwt));
    }

}
