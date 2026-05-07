package com.example.skripsi.models.company;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCompanyRequest {
    @NotBlank
    private String companyName;
    
    @NotBlank
    private String companyAbbreviation;
}

