package com.example.skripsi.models.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank
    private String companyAbbreviation;

    @Size(max = 35)
    @NotBlank
    private String website;

    @Size(max = 500)
    private String bio;

    private Boolean isPartner;

    @NotNull
    private Long subcategoryId;
}
