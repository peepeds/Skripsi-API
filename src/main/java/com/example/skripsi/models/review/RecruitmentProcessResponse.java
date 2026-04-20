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
public class RecruitmentProcessResponse {
    private List<ProcessItem> items;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProcessItem {
        private Long internshipHeaderId;
        private Long internshipDetailId;
        private String jobTitle;
        private String type;
        private String workScheme;
        private String admissionTrack;
        private String recruitmentDuration;
        private Integer durationMonths;
        private Integer year;
        private List<String> subCategories;
        private List<String> recruitmentSteps;
        private Integer interviewDifficulty;
        private String createdByName;
        private String exampleQuestions;
        private String selectionProcess;
        private String tipsTricks;
        private OffsetDateTime createdAt;
        private Long totalLikes;
        private Boolean verifiedReviewer;
    }
}
