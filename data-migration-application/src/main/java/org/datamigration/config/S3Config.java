package org.datamigration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@DependsOn("frontendDotEnvModel")
public class S3Config {

    @Value("${s3.region}")
    private String s3Region;

    @Value("${s3.endpoint}")
    private String s3Endpoint;

    @Value("${s3.accessKey}")
    private String s3AccessKey;

    @Value("${s3.secretKey}")
    private String s3SecretKey;

    @Value("${s3.pathStyleAccessEnabled}")
    private boolean pathStyleAccessEnabled;

    @Value("${s3.bucket}")
    private String bucket;

    @Bean
    S3Client s3Client() {
        final S3Configuration s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(pathStyleAccessEnabled)
                .build();
        return S3Client.builder()
                .region(Region.of(s3Region))
                .endpointOverride(URI.create(s3Endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey, s3SecretKey)))
                .serviceConfiguration(s3Configuration)
                .build();
    }

    @Bean
    S3Presigner s3Presigner() {
        final S3Configuration s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(pathStyleAccessEnabled)
                .build();
        return S3Presigner.builder()
                .region(Region.of(s3Region))
                .endpointOverride(URI.create(s3Endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey, s3SecretKey)))
                .serviceConfiguration(s3Configuration)
                .build();
    }

    @Bean
    ApplicationRunner initializeS3(S3Client s3Client) {
        return args -> {
            try {
                final HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                        .bucket(bucket)
                        .build();
                s3Client.headBucket(headBucketRequest);
            } catch (S3Exception ex) {
                final CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucket)
                        .build();
                s3Client.createBucket(createBucketRequest);
            }
        };
    }

}
