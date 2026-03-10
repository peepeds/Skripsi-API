package com.example.skripsi.models.minio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadUrlRequest {
    private String fileName;
    private String contentType;
}
