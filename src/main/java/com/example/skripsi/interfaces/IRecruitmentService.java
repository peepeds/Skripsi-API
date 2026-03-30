package com.example.skripsi.interfaces;

import com.example.skripsi.entities.RecruitmentStep;

import java.util.List;

public interface IRecruitmentService {
    RecruitmentStep createRecruitmentStep(RecruitmentStep step);

    RecruitmentStep updateRecruitmentStep(Long recruitmentStepId, RecruitmentStep stepDetails);

    void deleteRecruitmentStep(Long recruitmentStepId);

    RecruitmentStep getRecruitmentStepById(Long recruitmentStepId);

    List<RecruitmentStep> getRecruitmentStepsByInternshipHeader(Long internshipHeaderId);
}
