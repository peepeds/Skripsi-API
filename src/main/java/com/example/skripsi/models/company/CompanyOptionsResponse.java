package com.example.skripsi.models.company;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyOptionsResponse {
    private Long companyId;
    private String companyName;
    private String companyAbbreviation;
    private String website;
    private Boolean isPartner;
    private String companySlug;

    public CompanyOptionsResponse(Long companyId, String companyName, String companyAbbreviation, String companySlug) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyAbbreviation = companyAbbreviation;
        this.companySlug = companySlug;
    }

    public CompanyOptionsResponse(Long companyId, String companyName, String companyAbbreviation) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyAbbreviation = companyAbbreviation;
    }
}
