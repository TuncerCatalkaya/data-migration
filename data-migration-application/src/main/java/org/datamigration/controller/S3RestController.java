package org.datamigration.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.datamigration.model.CompletedPartModel;
import org.datamigration.usecase.S3Usecase;
import org.datamigration.usecase.model.GeneratePresignedUrlResponseModel;
import org.datamigration.usecase.model.InitiateMultipartUploadRequestModel;
import org.datamigration.usecase.model.S3ListResponseModel;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public InitiateMultipartUploadRequestModel initiateMultipartUpload(@RequestParam String bucket, @RequestParam String key) {
        return s3Usecase.initiateMultipartUpload(bucket, key);
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/multipart-upload/presigned-url")
    public GeneratePresignedUrlResponseModel generatePresignedUrlMultiPartUpload(@RequestParam String bucket, @RequestParam String key,
                                                                                 @RequestParam String uploadId, @RequestParam int partNumber) {
        return s3Usecase.generatePresignedUrlMultiPartUpload(bucket, key, uploadId, partNumber);
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping("/multipart-upload/complete")
    public void completeMultipartUpload(@RequestParam String bucket, @RequestParam String key, @RequestParam String uploadId, @RequestBody
    List<CompletedPartModel> completedParts) {
        s3Usecase.completeMultipartUpload(bucket, key, uploadId, completedParts);
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping("/multipart-upload/abort")
    public void completeMultipartUpload(@RequestParam String bucket, @RequestParam String key, @RequestParam String uploadId) {
        s3Usecase.abortMultipartUpload(bucket, key, uploadId);
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/objects")
    public List<S3ListResponseModel> listObjectsV2(@RequestParam String bucket, @RequestParam String projectId) {
        return s3Usecase.listObjectsV2(bucket, projectId);
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @DeleteMapping("/objects")
    public void deleteObject(@RequestParam String bucket, @RequestParam String key) {
        s3Usecase.deleteObject(bucket, key);
    }

}
