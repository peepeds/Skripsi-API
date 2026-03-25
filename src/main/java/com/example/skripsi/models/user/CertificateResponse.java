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
    private String issuer;
    private String certificatesUrl;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
