package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.*;
import com.example.skripsi.repositories.*;
import com.example.skripsi.interfaces.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RecruitmentService implements IRecruitmentService {
    private final RecruitmentStepRepository recruitmentStepRepository;

    public RecruitmentService(RecruitmentStepRepository recruitmentStepRepository) {
        this.recruitmentStepRepository = recruitmentStepRepository;
    }

    public RecruitmentStep createRecruitmentStep(RecruitmentStep step) {
        return recruitmentStepRepository.save(step);
    }

    public RecruitmentStep updateRecruitmentStep(Long recruitmentStepId, RecruitmentStep stepDetails) {
        RecruitmentStep step = recruitmentStepRepository.findById(recruitmentStepId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruitment step not found"));
        step.setStepName(stepDetails.getStepName());
        return recruitmentStepRepository.save(step);
    }

    public void deleteRecruitmentStep(Long recruitmentStepId) {
        recruitmentStepRepository.deleteById(recruitmentStepId);
    }

    public RecruitmentStep getRecruitmentStepById(Long recruitmentStepId) {
        return recruitmentStepRepository.findById(recruitmentStepId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruitment step not found"));
    }

    public List<RecruitmentStep> getRecruitmentStepsByInternshipHeader(Long internshipHeaderId) {
        return recruitmentStepRepository.findByInternshipHeaderId(internshipHeaderId);
    }
}
