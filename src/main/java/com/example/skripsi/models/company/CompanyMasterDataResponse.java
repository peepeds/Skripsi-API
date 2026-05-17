package com.example.skripsi.models.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyMasterDataResponse {
    private Long companyId;
    private String companyName;
    private String companyAbbreviation;
    private String companySlug;
    private OffsetDateTime companyCreatedAt;
    private Long companyCreatedBy;
    private OffsetDateTime companyUpdatedAt;
    private Long companyUpdatedBy;

    private Long companyProfileId;
    private String bio;
    private String website;
    private Boolean isPartner;
    private Long subcategoryId;
    private String subcategoryName;
    @Builder.Default
    private Long totalReviews = 0L;
    private OffsetDateTime profileCreatedAt;
    private Long profileCreatedBy;
    private OffsetDateTime profileUpdatedAt;
    private Long profileUpdatedBy;
}
