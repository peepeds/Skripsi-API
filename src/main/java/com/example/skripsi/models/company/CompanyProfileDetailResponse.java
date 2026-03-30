package com.example.skripsi.models.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProfileDetailResponse {
    private String companyName;
    private String companyAbbreviation;
    private String bio;
    private String website;
    private Boolean isPartner;
    private Long subcategoryId;
    private OffsetDateTime createdAt;
    private Long createdBy;
    private OffsetDateTime updatedAt;
    private Long updatedBy;
}

