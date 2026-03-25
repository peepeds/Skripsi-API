package com.example.skripsi.models.company;

import com.example.skripsi.entities.*;
import com.example.skripsi.validation.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReviewCompanyRequestRequest {

    @NotNull(message = "status is required")
    @ValidReviewStatus
    private CompanyRequestStatus status;

    @Size(max = 255)
    private String reviewNote;
}

