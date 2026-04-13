package com.example.skripsi.models.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecentReviewResponse {
    private List<ReviewItem> items;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReviewItem {
        private String testimony;
        private String createdBy;
        private Double averageRating;
        private String companyName;
        private String companyCategory;
        private String companySubCategory;
        private String companyWebsite;
        private String jobTitle;
        private OffsetDateTime createdAt;
    }
}
