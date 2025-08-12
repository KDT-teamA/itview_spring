package com.example.itview_spring.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3Uploader {
    
    private final S3Client s3Client;
    
    private static final String BUCKET_NAME = "itview";
    private static final Region REGION = Region.of("ap-northeast-2");
    private static final String ACCESS_KEY = "AKIAZFJGTHHGLJFUHYGK";
    private static final String SECRET_KEY = "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";

    public S3Uploader() {
        this.s3Client = S3Client.builder()
                .region(REGION)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                .build();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String key = getUniqueFileName(file.getOriginalFilename());
        byte[] resizedImage = resizeAndCompressImage(file);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(resizedImage));

        return key;
    }

    public String getFileUrl(String keyName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", BUCKET_NAME, REGION.id(), keyName);
    }

    public String getUniqueFileName(String originalFilename) {
        return System.currentTimeMillis() + "_" + originalFilename;
    }

    public byte[] resizeAndCompressImage(MultipartFile file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Thumbnails.of(file.getInputStream())
                .size(500, 500)
                .outputFormat("jpg")
                .outputQuality(0.4)
                .toOutputStream(baos);

        return baos.toByteArray();
    }
}