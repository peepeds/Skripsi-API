package com.example.skripsi.models.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubCategorySummaryResponse {
    private Long totalCompanies;
    private Long totalReviews;
    private Double avgRating;
    private Long totalPartnerCompanies;
}
