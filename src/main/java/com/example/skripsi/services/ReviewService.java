package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.ResourceNotFoundException;
import com.example.skripsi.exceptions.ValidationException;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.job.JobListItemResponse;
import com.example.skripsi.models.review.*;
import com.example.skripsi.repositories.*;
import com.example.skripsi.securities.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService implements IReviewService {

    private final InternshipHeaderRepository internshipHeaderRepository;
    private final InternshipDetailRepository internshipDetailRepository;
    private final RecruitmentStepRepository recruitmentStepRepository;
    private final InternshipJobSubCategoryRepository internshipJobSubCategoryRepository;
    private final CompanyRepository companyRepository;
    private final SecurityUtils securityUtils;
    private final SubCategoryRepository subCategoryRepository;
    private final AuditService auditService;

    public ReviewService(InternshipHeaderRepository internshipHeaderRepository,
                         InternshipDetailRepository internshipDetailRepository,
                         RecruitmentStepRepository recruitmentStepRepository,
                         InternshipJobSubCategoryRepository internshipJobSubCategoryRepository,
                         SubCategoryRepository subCategoryRepository,
                         CompanyRepository companyRepository,
                         SecurityUtils securityUtils,
                         AuditService auditService) {
        this.internshipHeaderRepository = internshipHeaderRepository;
        this.internshipDetailRepository = internshipDetailRepository;
        this.recruitmentStepRepository = recruitmentStepRepository;
        this.internshipJobSubCategoryRepository = internshipJobSubCategoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.companyRepository = companyRepository;
        this.securityUtils = securityUtils;
        this.auditService = auditService;
    }

    @Override
    public List<JobListItemResponse> searchJobOptions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        
        List<String> jobTitles = internshipHeaderRepository.searchJobTitles(query.trim());
        return jobTitles.stream()
                .map(jobTitle -> JobListItemResponse.builder()
                        .jobTitle(jobTitle)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ReviewResponse submitReview(Long companyId, CreateReviewRequest request, Long userId) {
        OffsetDateTime now = OffsetDateTime.now();

        validateAdditionalSubCategories(request.getSubCategoryIds());

        InternshipHeader internshipHeader = InternshipHeader.builder()
                .userId(userId)
                .companyId(companyId)
                .jobTitle(request.getJobTitle())
                .startYear(LocalDate.of(request.getYear(), 1, 1))
                .durationMonths(request.getDuration())
                .createdAt(now)
                .createdBy(userId)
                .build();

        InternshipHeader savedHeader = internshipHeaderRepository.save(internshipHeader);

        InternshipDetail internshipDetail = InternshipDetail.builder()
                .internshipHeaderId(savedHeader.getInternshipHeaderId())
                .type(request.getInternshipType())
                .scheme(request.getWorkScheme())
                .workCultureRating(request.getRatings().getWorkCulture())
                .learningOpportunityRating(request.getRatings().getLearningOpp())
                .mentorshipRating(request.getRatings().getMentorship())
                .benefitsRating(request.getRatings().getBenefit())
                .workLifeBalanceRating(request.getRatings().getWorkLifeBalance())
                .interviewDifficultyRating(request.getInterviewDifficulty())
                .testimony(request.getTestimony())
                .pros(request.getPros())
                .cons(request.getCons())
                .createdAt(now)
                .createdBy(userId)
                .build();

        InternshipDetail savedDetail = internshipDetailRepository.save(internshipDetail);

        if (request.getRecruitmentProcess() != null && !request.getRecruitmentProcess().isEmpty()) {
            for (String stepName : request.getRecruitmentProcess()) {
                RecruitmentStep recruitmentStep = RecruitmentStep.builder()
                        .internshipHeaderId(savedHeader.getInternshipHeaderId())
                        .stepName(stepName)
                        .build();
                recruitmentStepRepository.save(recruitmentStep);
            }
        }

        if (request.getSubCategoryIds() != null && !request.getSubCategoryIds().isEmpty()) {
            for (Long subCategoryId : request.getSubCategoryIds()) {
                InternshipJobSubCategory jobSubCategory = InternshipJobSubCategory.builder()
                        .internshipHeaderId(savedHeader.getInternshipHeaderId())
                        .subCategoryId(subCategoryId)
                        .createdAt(now)
                        .createdBy(userId)
                        .build();
                internshipJobSubCategoryRepository.save(jobSubCategory);
            }
        }

        auditService.record("INTERNSHIP_REVIEW", savedHeader.getInternshipHeaderId(), "SUBMITTED", userId);

        return ReviewResponse.builder()
                .internshipHeaderId(savedHeader.getInternshipHeaderId())
                .internshipDetailId(savedDetail.getInternshipDetailId())
                .createdAt(now)
                .message("Review submitted successfully")
                .build();
    }

    @Override
    public ReviewResponse submitReview(String slug, CreateReviewRequest request) {
        Company company = companyRepository.findByCompanySlug(slug);
        if (company == null) {
            throw new ResourceNotFoundException("Company with slug '" + slug + "' not found");
        }

        Long userId = securityUtils.getCurrentUserId();
        return submitReview(company.getCompanyId(), request, userId);
    }

    private void validateAdditionalSubCategories(List<Long> subCategoryIds) {
        if (subCategoryIds == null || subCategoryIds.isEmpty()) {
            return;
        }

        if (subCategoryIds.size() > 3) {
            throw new ValidationException("Maximum 3 additional subcategories allowed");
        }

        Set<Long> idSet = new HashSet<>(subCategoryIds);
        if (idSet.size() != subCategoryIds.size()) {
            throw new ValidationException("Duplicate subcategory IDs not allowed");
        }

        for (Long subCategoryId : subCategoryIds) {
            if (!subCategoryRepository.existsById(subCategoryId)) {
                throw new ResourceNotFoundException("SubCategory not found with id: " + subCategoryId);
            }
        }
    }
}
