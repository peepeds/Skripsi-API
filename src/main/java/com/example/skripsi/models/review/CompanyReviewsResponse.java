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
public class CompanyReviewsResponse {
    private List<ReviewItem> items;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReviewItem {
        private Long internshipHeaderId;
        private Long internshipDetailId;
        private String jobTitle;
        private String type;
        private String workScheme;
        private Integer durationMonths;
        private Integer year;
        private List<String> subCategories;
        private Ratings ratings;
        private List<String> recruitmentSteps;
        private Integer interviewDifficulty;
        private String testimony;
        private String pros;
        private String cons;
        private OffsetDateTime createdAt;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Ratings {
        private Integer workCulture;
        private Integer learningOpp;
        private Integer mentorship;
        private Integer benefit;
        private Integer workLifeBalance;
    }
}
