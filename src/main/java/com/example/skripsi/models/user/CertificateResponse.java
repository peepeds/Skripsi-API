package com.example.skripsi.models.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {
    private Long userCertificateId;
    private Long issuer;
    private String issuerName;
    private String certificatesUrl;
    private String certificateName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
