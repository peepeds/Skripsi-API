package com.example.skripsi.models.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecruitmentProcessSummaryResponse {
    private Difficulty difficulty;
    private Statistics statistics;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Difficulty {
        private Double rating;
        private Long count;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Statistics {
        private Map<String, String> admissionTrack;
        private String recruitmentDuration;
        private Map<String, String> frequentSelectionProcess;
    }
}
