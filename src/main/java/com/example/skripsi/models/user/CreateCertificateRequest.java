package com.example.skripsi.models.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCertificateRequest {
    private Long issuer;
    private String certificateUrl;
    private String certificateName;
    private Long fileSize;
}
