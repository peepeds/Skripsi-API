package com.example.skripsi.models.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MyReviewsResponse {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReviewItem {
        private String companyName;
        private String jobTitle;
        private String testimony;
        private String recruitmentUrl;
        private String reviewUrl;
    }
}
