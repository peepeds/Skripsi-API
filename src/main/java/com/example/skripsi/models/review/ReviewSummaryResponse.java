package com.example.skripsi.models.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewSummaryResponse {
    private InformationDetails informationDetails;
    private Ratings ratings;
    private RecruitmentProcesses recruitmentProcesses;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InformationDetails {
        private String type;
        private List<String> workScheme;
        private String duration;
        private List<String> subCategories;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Ratings {
        private Double workCulture;
        private Double learningOpp;
        private Double mentorship;
        private Double benefit;
        private Double workLifeBalance;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecruitmentProcesses {
        private Double rating;
        private List<String> steps;
    }
}
