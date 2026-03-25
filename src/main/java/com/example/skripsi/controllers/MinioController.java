package com.example.skripsi.controllers;

import com.example.skripsi.models.*;
import com.example.skripsi.services.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/minio")
public class MinioController {

    private final MinioService minioService;

    public MinioController(MinioService minioService) {
        this.minioService = minioService;
    }

    @GetMapping("/upload-url")
    public WebResponse<?> getUploadUrl(@RequestParam("extension") String extension) throws Exception {
        var result = minioService.getPresignedUploadUrl(extension);
        return WebResponse.builder()
                .success(true)
                .message("Successfully generated upload URL")
                .result(result)
                .build();
    }
}
