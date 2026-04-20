package com.example.skripsi.interfaces;

import java.util.Map;

public interface IMinioService {
    String generateFileName(String extension);

    Map<String, String> getPresignedUploadUrl(String extension) throws Exception;

    String getPresignedViewUrl(String fileName) throws Exception;
}
