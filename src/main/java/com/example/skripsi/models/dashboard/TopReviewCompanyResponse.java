package com.example.skripsi.models.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopReviewCompanyResponse {
    private String companyName;
    private Long totalReviews;
}
