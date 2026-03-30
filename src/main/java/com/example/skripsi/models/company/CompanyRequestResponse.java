package com.example.skripsi.models.company;

import com.example.skripsi.entities.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyRequestResponse {
    private Long companyRequestId;
    private String companyName;
    private String companyAbbreviation;
    private String website;
    private Boolean isPartner;
    private Long subcategoryId;
    private CompanyRequestStatus status;
    private OffsetDateTime createdAt;
    private String createdBy;
    private OffsetDateTime reviewedAt;
    private String reviewedBy;
    private String reviewNote;
}
