package com.example.skripsi.models.company;

import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateCompanyRequest {
    @Size(min = 3, max = 65)
    private String companyName;

    @Size(max = 15)
    private String companyAbbreviation;

    @Size(max = 500)
    private String bio;

    @Size(max = 35)
    private String website;

    private Long subcategoryId;

    private Boolean isPartner;
}
