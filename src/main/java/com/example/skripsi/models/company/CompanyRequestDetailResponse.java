package com.example.skripsi.models.company;

import com.example.skripsi.entities.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyRequestDetailResponse {

    private RequestDetails requestDetails;
    private ReviewInformation reviewInformation;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RequestDetails {
        private Long id;
        private String companyName;
        private String companyAbbreviation;
        private String website;
        private Long subcategoryId;
        private String submittedBy;
        private OffsetDateTime submittedAt;
        private List<DocumentResponse> documents;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReviewInformation {
        private CompanyRequestStatus status;
        private String reviewNote;
        private String reviewedBy;
        private OffsetDateTime reviewedAt;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentResponse {
        private String fileName;
        private String url;
    }
}
