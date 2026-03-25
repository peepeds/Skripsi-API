package com.example.skripsi.models.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateCompanyRequestRequest {
    @NotBlank
    @Size(min = 3, max = 65)
    private String companyName;

    @Size(max = 15)
    private String companyAbbreviation;

    @Size(max = 35)
    private String website;

    private Boolean isPartner;
}
