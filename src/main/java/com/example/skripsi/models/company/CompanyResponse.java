package com.example.skripsi.models.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyResponse {
    private Long companyId;
    private String companyName;
    private String companyAbbreviation;
    private String companySlug;
}
