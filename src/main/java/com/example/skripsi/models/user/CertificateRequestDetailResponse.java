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
public class CertificateRequestDetailResponse {
    private RequestDetails requestDetails;
    private ReviewInformation reviewInformation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestDetails {
        private Long requestId;
        private String certificateName;
        private String certificatesUrl;
        private Long fileSize;
        private OffsetDateTime submittedAt;
        private String submittedBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewInformation {
        private String status;
        private OffsetDateTime reviewedAt;
        private String reviewNote;
        private String reviewedBy;
    }
}
