package com.example.skripsi.services;

import com.example.skripsi.configs.MinioConfig;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MinioService {

    private static final SecureRandom random = new SecureRandom();
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    public MinioService(MinioClient minioClient, MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
    }

    public String generateFileName(String extension) {
        long timestamp = Instant.now().toEpochMilli();
        long msb = (timestamp << 16) | (7L << 12) | (random.nextLong() & 0xFFF);
        long lsb = random.nextLong();
        UUID uuid = new UUID(msb, lsb);
        return uuid.toString() + "." + extension;
    }

    public Map<String, String> getPresignedUploadUrl(String extension) throws Exception {
        String fileName = generateFileName(extension);

        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(minioConfig.getBucketName())
                        .object(fileName)
                        .expiry(300)
                        .build()
        );

        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("fileName", fileName);
        return result;
    }
}
