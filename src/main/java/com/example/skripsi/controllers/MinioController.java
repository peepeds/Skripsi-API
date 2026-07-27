package com.example.skripsi.controllers;

import com.example.skripsi.interfaces.IMinioService;
import com.example.skripsi.models.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/minio")
public class MinioController {

    private final IMinioService minioService;

    public MinioController(IMinioService minioService) {
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
