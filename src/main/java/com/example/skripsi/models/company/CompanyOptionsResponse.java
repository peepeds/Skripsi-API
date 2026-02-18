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
}
