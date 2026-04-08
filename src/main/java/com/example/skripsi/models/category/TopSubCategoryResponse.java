package com.example.skripsi.models.category;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopSubCategoryResponse {
    private String subcategoryName;
    private String url;
    private Long totalReviews;
}
