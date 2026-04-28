package com.example.skripsi.models.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertificateRequestListResponse {
    private Long requestId;
    private String certificateName;
    private String status;
    private OffsetDateTime createdAt;
    private String submittedBy;
    private OffsetDateTime reviewedAt;
    private String reviewedBy;
    private String reviewNote;
}
