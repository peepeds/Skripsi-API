package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.*;
import com.example.skripsi.repositories.*;
import com.example.skripsi.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class RecruitmentService implements IRecruitmentService {
    private final RecruitmentStepRepository recruitmentStepRepository;

    public RecruitmentService(RecruitmentStepRepository recruitmentStepRepository) {
        this.recruitmentStepRepository = recruitmentStepRepository;
    }

    public RecruitmentStep createRecruitmentStep(RecruitmentStep step) {
        log.info("[createRecruitmentStep] stepCode={}", step.getStepCode());
        RecruitmentStep saved = recruitmentStepRepository.save(step);
        log.info("[createRecruitmentStep] created recruitmentStepId={}", saved.getRecruitmentStepId());
        return saved;
    }

    public RecruitmentStep updateRecruitmentStep(Long recruitmentStepId, RecruitmentStep stepDetails) {
        log.info("[updateRecruitmentStep] recruitmentStepId={} newStepCode={}", recruitmentStepId, stepDetails.getStepCode());
        RecruitmentStep step = recruitmentStepRepository.findById(recruitmentStepId)
                .orElseThrow(() -> {
                    log.warn("[updateRecruitmentStep] step not found recruitmentStepId={}", recruitmentStepId);
                    return new ResourceNotFoundException("Recruitment step not found");
                });
        step.setStepCode(stepDetails.getStepCode());
        return recruitmentStepRepository.save(step);
    }

    public void deleteRecruitmentStep(Long recruitmentStepId) {
        log.info("[deleteRecruitmentStep] recruitmentStepId={}", recruitmentStepId);
        recruitmentStepRepository.deleteById(recruitmentStepId);
    }

    public RecruitmentStep getRecruitmentStepById(Long recruitmentStepId) {
        return recruitmentStepRepository.findById(recruitmentStepId)
                .orElseThrow(() -> {
                    log.warn("[getRecruitmentStepById] step not found recruitmentStepId={}", recruitmentStepId);
                    return new ResourceNotFoundException("Recruitment step not found");
                });
    }

    public List<RecruitmentStep> getRecruitmentStepsByInternshipHeader(Long internshipHeaderId) {
        log.info("[getRecruitmentStepsByInternshipHeader] internshipHeaderId={}", internshipHeaderId);
        return recruitmentStepRepository.findByInternshipHeaderId(internshipHeaderId);
    }
}
