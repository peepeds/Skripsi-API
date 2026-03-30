package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.*;
import com.example.skripsi.repositories.*;
import com.example.skripsi.interfaces.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InternshipService implements IInternshipService {
    private final InternshipHeaderRepository internshipHeaderRepository;
    private final InternshipDetailRepository internshipDetailRepository;

    public InternshipService(InternshipHeaderRepository internshipHeaderRepository,
                             InternshipDetailRepository internshipDetailRepository) {
        this.internshipHeaderRepository = internshipHeaderRepository;
        this.internshipDetailRepository = internshipDetailRepository;
    }

    public InternshipHeader createInternshipHeader(InternshipHeader header) {
        return internshipHeaderRepository.save(header);
    }

    public InternshipHeader updateInternshipHeader(Long internshipHeaderId, InternshipHeader headerDetails) {
        InternshipHeader header = internshipHeaderRepository.findById(internshipHeaderId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship header not found"));
        header.setStartYear(headerDetails.getStartYear());
        header.setDurationMonths(headerDetails.getDurationMonths());
        header.setJobTitle(headerDetails.getJobTitle());
        header.setUpdatedAt(headerDetails.getUpdatedAt());
        header.setUpdatedBy(headerDetails.getUpdatedBy());
        return internshipHeaderRepository.save(header);
    }

    public void deleteInternshipHeader(Long internshipHeaderId) {
        internshipHeaderRepository.deleteById(internshipHeaderId);
    }

    public InternshipHeader getInternshipHeaderById(Long internshipHeaderId) {
        return internshipHeaderRepository.findById(internshipHeaderId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship header not found"));
    }

    public List<InternshipHeader> getInternshipsByUser(Long userId) {
        return internshipHeaderRepository.findByUserId(userId);
    }

    public List<InternshipHeader> getInternshipsByCompany(Long companyId) {
        return internshipHeaderRepository.findByCompanyId(companyId);
    }

    public Optional<InternshipHeader> getInternshipByUserAndCompany(Long userId, Long companyId) {
        return internshipHeaderRepository.findByUserIdAndCompanyId(userId, companyId);
    }

    public InternshipDetail createInternshipDetail(InternshipDetail detail) {
        return internshipDetailRepository.save(detail);
    }

    public InternshipDetail updateInternshipDetail(Long internshipDetailId, InternshipDetail detailsData) {
        InternshipDetail detail = internshipDetailRepository.findById(internshipDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship detail not found"));
        detail.setType(detailsData.getType());
        detail.setScheme(detailsData.getScheme());
        detail.setWorkCultureRating(detailsData.getWorkCultureRating());
        detail.setWorkLifeBalanceRating(detailsData.getWorkLifeBalanceRating());
        detail.setLearningOpportunityRating(detailsData.getLearningOpportunityRating());
        detail.setMentorshipRating(detailsData.getMentorshipRating());
        detail.setBenefitsRating(detailsData.getBenefitsRating());
        detail.setInterviewDifficultyRating(detailsData.getInterviewDifficultyRating());
        detail.setTestimony(detailsData.getTestimony());
        detail.setPros(detailsData.getPros());
        detail.setCons(detailsData.getCons());
        detail.setUpdatedAt(detailsData.getUpdatedAt());
        detail.setUpdatedBy(detailsData.getUpdatedBy());
        return internshipDetailRepository.save(detail);
    }

    public InternshipDetail getInternshipDetailById(Long internshipDetailId) {
        return internshipDetailRepository.findById(internshipDetailId)
                .orElseThrow(() -> new RuntimeException("Internship detail not found"));
    }

    public InternshipDetail getInternshipDetailByHeaderId(Long internshipHeaderId) {
        return internshipDetailRepository.findByInternshipHeaderId(internshipHeaderId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship detail not found"));
    }

    public void deleteInternshipDetail(Long internshipDetailId) {
        internshipDetailRepository.deleteById(internshipDetailId);
    }
}
