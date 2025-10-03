package com.marimo.server.global.adapter;

import com.marimo.server.domain.order.enums.FileType;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Adapter {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    public String issuePresignedUrl(final String s3Key, final FileType fileType) {
        return issuePresignedUrl(
                s3Key,
                fileType.getMimeType(),
                Duration.ofMinutes(15)
        );
    }

    public String issuePresignedUrl(
            final String s3Key,
            final String contentType,
            final Duration ttl
    ) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(
                b -> b
                        .putObjectRequest(putObjectRequest)
                        .signatureDuration(ttl)
        );

        return presignedPutObjectRequest.url().toString();
    }
}
