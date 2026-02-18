package com.example.skripsi.models.company;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyPreviewResponse {
    private Long approxCompanies;
    private List<CompanyOptionsResponse> companies;
}
