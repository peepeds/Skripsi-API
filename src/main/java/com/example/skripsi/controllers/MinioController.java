package com.example.skripsi.controllers;

import com.example.skripsi.configs.MinioConfig;
import com.example.skripsi.models.WebResponse;
import com.example.skripsi.services.MinioService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/minio")
public class MinioController {

    private final MinioClient minioClient;
    private final MinioService minioService;
    private final MinioConfig minioConfig;

    public MinioController(MinioConfig minioConfig, MinioService minioService) {
        this.minioConfig = minioConfig;
        this.minioClient = MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .build();
        this.minioService = minioService;
    }

    @GetMapping("/upload-url")
    public WebResponse<?> getUploadUrl(@RequestParam("extension") String extension) throws Exception {
        String fileName = minioService.generateFileName(extension);

        // Generate signed PUT URL, berlaku 5 menit
        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(minioConfig.getBucketName())
                        .object(fileName)
                        .expiry(300) // 5 menit
                        .build()
        );

        return WebResponse.builder()
                .success(true)
                .message("Successfully generated upload URL")
                .result(Map.of("url", url, "fileName", fileName))
                .build();
    }
}
