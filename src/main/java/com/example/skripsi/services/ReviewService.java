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
import java.util.*;
import java.util.function.Function;
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
            List<RecruitmentStep> recruitmentSteps = new ArrayList<>();
            for (String stepName : request.getRecruitmentProcess()) {
                RecruitmentStep recruitmentStep = RecruitmentStep.builder()
                        .internshipHeaderId(savedHeader.getInternshipHeaderId())
                        .stepName(stepName)
                        .build();
                recruitmentSteps.add(recruitmentStep);
            }
            recruitmentStepRepository.saveAll(recruitmentSteps);
        }

        if (request.getSubCategoryIds() != null && !request.getSubCategoryIds().isEmpty()) {
            List<InternshipJobSubCategory> jobSubCategories = new ArrayList<>();
            for (Long subCategoryId : request.getSubCategoryIds()) {
                InternshipJobSubCategory jobSubCategory = InternshipJobSubCategory.builder()
                        .internshipHeaderId(savedHeader.getInternshipHeaderId())
                        .subCategoryId(subCategoryId)
                        .createdAt(now)
                        .createdBy(userId)
                        .build();
                jobSubCategories.add(jobSubCategory);
            }
            internshipJobSubCategoryRepository.saveAll(jobSubCategories);
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

    public ReviewSummaryResponse getCompanySummary(String slug) {
        Company company = companyRepository.findByCompanySlug(slug);
        if (company == null) {
            throw new ResourceNotFoundException("Company with slug '" + slug + "' not found");
        }

        Long companyId = company.getCompanyId();

        List<InternshipHeader> headers = internshipHeaderRepository.findByCompanyId(companyId);
        if (headers.isEmpty()) {
            return ReviewSummaryResponse.builder()
                    .informationDetails(ReviewSummaryResponse.InformationDetails.builder().build())
                    .ratings(ReviewSummaryResponse.Ratings.builder().build())
                    .recruitmentProcesses(ReviewSummaryResponse.RecruitmentProcesses.builder().build())
                    .build();
        }

        List<InternshipDetail> details = internshipDetailRepository.findAllDetailsByCompanyId(companyId);

        String type = getMostFrequentType(details);
        String duration = getDurationRange(headers);
        List<String> workSchemes = getWorkSchemesSortedByFrequency(details);
        List<String> subCategories = getTop5SubCategories(companyId);
        ReviewSummaryResponse.Ratings ratings = aggregateRatings(details);
        ReviewSummaryResponse.RecruitmentProcesses recruitmentProcesses = aggregateRecruitmentProcesses(companyId);

        return ReviewSummaryResponse.builder()
                .informationDetails(ReviewSummaryResponse.InformationDetails.builder()
                        .type(type)
                        .workScheme(workSchemes)
                        .duration(duration)
                        .subCategories(subCategories)
                        .build())
                .ratings(ratings)
                .recruitmentProcesses(recruitmentProcesses)
                .build();
    }

    private String getMostFrequentType(List<InternshipDetail> details) {
        return details.stream()
                .collect(Collectors.groupingBy(InternshipDetail::getType, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private String getDurationRange(List<InternshipHeader> headers) {
        if (headers.isEmpty()) {
            return null;
        }

        Integer minDuration = headers.stream()
                .map(InternshipHeader::getDurationMonths)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(null);

        Integer maxDuration = headers.stream()
                .map(InternshipHeader::getDurationMonths)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);

        if (minDuration == null || maxDuration == null) {
            return null;
        }

        return minDuration.equals(maxDuration) 
                ? minDuration + " months" 
                : minDuration + " - " + maxDuration + " months";
    }

    private List<String> getWorkSchemesSortedByFrequency(List<InternshipDetail> details) {
        return details.stream()
                .collect(Collectors.groupingBy(InternshipDetail::getScheme, Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(Map.Entry::getKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<String> getTop5SubCategories(Long companyId) {
        List<InternshipJobSubCategory> jobSubCategories = internshipJobSubCategoryRepository.findAllSubCategoriesByCompanyId(companyId);

        List<Long> subCategoryIds = jobSubCategories.stream()
                .collect(Collectors.groupingBy(InternshipJobSubCategory::getSubCategoryId, Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (subCategoryIds.isEmpty()) {
            return List.of();
        }

        Map<Long, SubCategory> subcatMap = subCategoryRepository.findBySubCategoryIds(subCategoryIds).stream()
                .collect(Collectors.toMap(SubCategory::getSubCategoryId, Function.identity()));

        return subCategoryIds.stream()
                .map(subCategoryId -> {
                    SubCategory subCat = subcatMap.get(subCategoryId);
                    return subCat != null ? subCat.getSubCategoryName() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ReviewSummaryResponse.Ratings aggregateRatings(List<InternshipDetail> details) {
        double workCultureAvg = details.stream()
                .map(InternshipDetail::getWorkCultureRating)
                .filter(Objects::nonNull)
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);

        double learningOppAvg = details.stream()
                .map(InternshipDetail::getLearningOpportunityRating)
                .filter(Objects::nonNull)
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);

        double mentorshipAvg = details.stream()
                .map(InternshipDetail::getMentorshipRating)
                .filter(Objects::nonNull)
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);

        double benefitAvg = details.stream()
                .map(InternshipDetail::getBenefitsRating)
                .filter(Objects::nonNull)
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);

        double workLifeBalanceAvg = details.stream()
                .map(InternshipDetail::getWorkLifeBalanceRating)
                .filter(Objects::nonNull)
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);

        return ReviewSummaryResponse.Ratings.builder()
                .workCulture(workCultureAvg)
                .learningOpp(learningOppAvg)
                .mentorship(mentorshipAvg)
                .benefit(benefitAvg)
                .workLifeBalance(workLifeBalanceAvg)
                .build();
    }

    private ReviewSummaryResponse.RecruitmentProcesses aggregateRecruitmentProcesses(Long companyId) {
        List<RecruitmentStep> steps = recruitmentStepRepository.findAllStepsByCompanyId(companyId);

        if (steps.isEmpty()) {
            return ReviewSummaryResponse.RecruitmentProcesses.builder()
                    .rating(null)
                    .steps(List.of())
                    .build();
        }

        List<Long> headerIds = steps.stream()
                .map(RecruitmentStep::getInternshipHeaderId)
                .distinct()
                .collect(Collectors.toList());

        List<InternshipDetail> details = internshipDetailRepository.findByInternshipHeaderIds(headerIds);

        OptionalDouble ratingOptional = details.stream()
                .map(InternshipDetail::getInterviewDifficultyRating)
                .filter(Objects::nonNull)
                .mapToDouble(Integer::doubleValue)
                .average();

        Double ratingAvg = ratingOptional.isPresent() ? ratingOptional.getAsDouble() : null;

        List<String> stepNames = steps.stream()
                .map(RecruitmentStep::getStepName)
                .distinct()
                .collect(Collectors.toList());

        return ReviewSummaryResponse.RecruitmentProcesses.builder()
                .rating(ratingAvg)
                .steps(stepNames)
                .build();
    }

    public CompanyReviewsResponse getCompanyReviews(String slug) {
        Company company = companyRepository.findByCompanySlug(slug);
        if (company == null) {
            throw new ResourceNotFoundException("Company with slug '" + slug + "' not found");
        }

        Long companyId = company.getCompanyId();
        List<InternshipHeader> headers = internshipHeaderRepository.findByCompanyId(companyId);
        
        if (headers.isEmpty()) {
            return CompanyReviewsResponse.builder()
                    .items(List.of())
                    .build();
        }

        List<InternshipDetail> details = internshipDetailRepository.findAllDetailsByCompanyId(companyId);
        Map<Long, InternshipDetail> detailMap = details.stream()
                .collect(Collectors.toMap(InternshipDetail::getInternshipHeaderId, detail -> detail));

        List<InternshipJobSubCategory> jobSubCategories = internshipJobSubCategoryRepository.findAllSubCategoriesByCompanyId(companyId);

        List<Long> allSubCategoryIds = jobSubCategories.stream()
                .map(InternshipJobSubCategory::getSubCategoryId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, SubCategory> subcategoryMap = subCategoryRepository.findBySubCategoryIds(allSubCategoryIds).stream()
                .collect(Collectors.toMap(SubCategory::getSubCategoryId, Function.identity()));

        Map<Long, List<String>> subCategoriesMap = jobSubCategories.stream()
                .collect(Collectors.groupingBy(
                        InternshipJobSubCategory::getInternshipHeaderId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .map(ijsc -> {
                                            SubCategory subCat = subcategoryMap.get(ijsc.getSubCategoryId());
                                            return subCat != null ? subCat.getSubCategoryName() : null;
                                        })
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList())
                        )
                ));

        List<RecruitmentStep> allSteps = recruitmentStepRepository.findAllStepsByCompanyId(companyId);
        Map<Long, List<String>> stepsMap = allSteps.stream()
                .collect(Collectors.groupingBy(
                        RecruitmentStep::getInternshipHeaderId,
                        Collectors.mapping(RecruitmentStep::getStepName, Collectors.toList())
                ));

        List<CompanyReviewsResponse.ReviewItem> items = headers.stream()
                .map(header -> {
                    InternshipDetail detail = detailMap.get(header.getInternshipHeaderId());
                    
                    CompanyReviewsResponse.Ratings ratings = detail != null 
                        ? CompanyReviewsResponse.Ratings.builder()
                                .workCulture(detail.getWorkCultureRating())
                                .learningOpp(detail.getLearningOpportunityRating())
                                .mentorship(detail.getMentorshipRating())
                                .benefit(detail.getBenefitsRating())
                                .workLifeBalance(detail.getWorkLifeBalanceRating())
                                .build()
                        : null;

                    Integer year = header.getStartYear() != null ? header.getStartYear().getYear() : null;

                    return CompanyReviewsResponse.ReviewItem.builder()
                            .internshipHeaderId(header.getInternshipHeaderId())
                            .internshipDetailId(detail != null ? detail.getInternshipDetailId() : null)
                            .jobTitle(header.getJobTitle())
                            .type(detail != null ? detail.getType() : null)
                            .workScheme(detail != null ? detail.getScheme() : null)
                            .durationMonths(header.getDurationMonths())
                            .year(year)
                            .subCategories(subCategoriesMap.getOrDefault(header.getInternshipHeaderId(), List.of()))
                            .ratings(ratings)
                            .recruitmentSteps(stepsMap.getOrDefault(header.getInternshipHeaderId(), List.of()))
                            .interviewDifficulty(detail != null ? detail.getInterviewDifficultyRating() : null)
                            .testimony(detail != null ? detail.getTestimony() : null)
                            .pros(detail != null ? detail.getPros() : null)
                            .cons(detail != null ? detail.getCons() : null)
                            .createdAt(detail != null ? detail.getCreatedAt() : null)
                            .build();
                })
                .collect(Collectors.toList());

        return CompanyReviewsResponse.builder()
                .items(items)
                .build();
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

    @Override
    public RecentReviewResponse getRecentReviews() {
        List<Object[]> results = internshipDetailRepository.findTop10RecentReviews();
        
        List<RecentReviewResponse.ReviewItem> items = results.stream()
                .map(row -> {
                    Object createdAtValue = row[7];
                    OffsetDateTime createdAt = null;
                    if (createdAtValue instanceof OffsetDateTime) {
                        createdAt = (OffsetDateTime) createdAtValue;
                    } else if (createdAtValue instanceof java.time.Instant) {
                        createdAt = ((java.time.Instant) createdAtValue).atOffset(java.time.ZoneOffset.UTC);
                    }
                    
                    return RecentReviewResponse.ReviewItem.builder()
                            .testimony((String) row[0])
                            .createdBy((String) row[1])
                            .averageRating(row[2] != null ? ((Number) row[2]).doubleValue() : 0.0)
                            .companyName((String) row[3])
                            .companyCategory((String) row[4])
                            .companyWebsite((String) row[5])
                            .jobTitle((String) row[6])
                            .createdAt(createdAt)
                            .build();
                })
                .collect(Collectors.toList());
        
        return RecentReviewResponse.builder()
                .items(items)
                .build();
    }
}
