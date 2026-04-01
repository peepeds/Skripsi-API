package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.ResourceNotFoundException;
import com.example.skripsi.exceptions.ValidationException;
import com.example.skripsi.repositories.projections.CompanyRatingProjection;
import com.example.skripsi.repositories.projections.CompanyReviewCountProjection;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.job.JobListItemResponse;
import com.example.skripsi.models.review.*;
import com.example.skripsi.models.CursorPageResponse;
import com.example.skripsi.models.constant.*;
import com.example.skripsi.repositories.*;
import com.example.skripsi.repositories.projections.RecentReviewProjection;
import com.example.skripsi.securities.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService implements IReviewService {

    private static final String DURATION_UNIT = " months";
    private static final String DURATION_RANGE_SEPARATOR = " - ";

    private final InternshipHeaderRepository internshipHeaderRepository;
    private final InternshipDetailRepository internshipDetailRepository;
    private final RecruitmentStepRepository recruitmentStepRepository;
    private final InternshipJobSubCategoryRepository internshipJobSubCategoryRepository;
    private final ICompanyService companyService;
    private final SecurityUtils securityUtils;
    private final ICategoryService categoryService;
    private final AuditService auditService;

    public ReviewService(InternshipHeaderRepository internshipHeaderRepository,
                         InternshipDetailRepository internshipDetailRepository,
                         RecruitmentStepRepository recruitmentStepRepository,
                         InternshipJobSubCategoryRepository internshipJobSubCategoryRepository,
                         ICategoryService categoryService,
                         ICompanyService companyService,
                         SecurityUtils securityUtils,
                         AuditService auditService) {
        this.internshipHeaderRepository = internshipHeaderRepository;
        this.internshipDetailRepository = internshipDetailRepository;
        this.recruitmentStepRepository = recruitmentStepRepository;
        this.internshipJobSubCategoryRepository = internshipJobSubCategoryRepository;
        this.categoryService = categoryService;
        this.companyService = companyService;
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

        auditService.record(EntityTypeConstants.INTERNSHIP_REVIEW, savedHeader.getInternshipHeaderId(), ActionConstants.SUBMITTED, userId);

        return ReviewResponse.builder()
                .internshipHeaderId(savedHeader.getInternshipHeaderId())
                .internshipDetailId(savedDetail.getInternshipDetailId())
                .createdAt(now)
                .message(MessageConstants.Success.REVIEW_SUBMITTED_SUCCESSFULLY)
                .build();
    }

    @Override
    public ReviewResponse submitReview(String slug, CreateReviewRequest request) {
        Long companyId = companyService.getCompanyIdBySlug(slug);
        Long userId = securityUtils.getCurrentUserId();
        return submitReview(companyId, request, userId);
    }

    public ReviewSummaryResponse getCompanySummary(String slug) {
        Long companyId = companyService.getCompanyIdBySlug(slug);

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
                ? minDuration + DURATION_UNIT
                : minDuration + DURATION_RANGE_SEPARATOR + maxDuration + DURATION_UNIT;
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

        Map<Long, String> subcatNameMap = categoryService.getSubCategoryNameMap(subCategoryIds);

        return subCategoryIds.stream()
                .map(subcatNameMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ReviewSummaryResponse.Ratings aggregateRatings(List<InternshipDetail> details) {
        return ReviewSummaryResponse.Ratings.builder()
                .workCulture(averageRating(details, InternshipDetail::getWorkCultureRating))
                .learningOpp(averageRating(details, InternshipDetail::getLearningOpportunityRating))
                .mentorship(averageRating(details, InternshipDetail::getMentorshipRating))
                .benefit(averageRating(details, InternshipDetail::getBenefitsRating))
                .workLifeBalance(averageRating(details, InternshipDetail::getWorkLifeBalanceRating))
                .build();
    }

    private double averageRating(List<InternshipDetail> details,
                                  Function<InternshipDetail, Integer> mapper) {
        return details.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);
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

    @Override
    public CursorPageResponse<CompanyReviewsResponse.ReviewItem> getCompanyReviews(String slug, String order, Long cursor, int limit) {
        Long companyId = companyService.getCompanyIdBySlug(slug);
        List<InternshipHeader> headers = internshipHeaderRepository.findByCompanyId(companyId);
        
        if (headers.isEmpty()) {
            return CursorPageResponse.<CompanyReviewsResponse.ReviewItem>builder()
                    .result(List.of())
                    .meta(CursorPageResponse.Meta.builder()
                            .nextCursor(null)
                            .previousCursor(null)
                            .size(0)
                            .hasMore(false)
                            .build())
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

        Map<Long, String> subcategoryNameMap = categoryService.getSubCategoryNameMap(allSubCategoryIds);

        Map<Long, List<String>> subCategoriesMap = jobSubCategories.stream()
                .collect(Collectors.groupingBy(
                        InternshipJobSubCategory::getInternshipHeaderId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .map(jobSubCategory -> subcategoryNameMap.get(jobSubCategory.getSubCategoryId()))
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
                .filter(header -> cursor == null || header.getInternshipHeaderId() > cursor)
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
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());

        List<CompanyReviewsResponse.ReviewItem> paginatedItems = items.stream()
                .limit(limit + 1)
                .collect(Collectors.toList());

        boolean hasMore = paginatedItems.size() > limit;
        if (hasMore) {
            paginatedItems = paginatedItems.subList(0, limit);
        }

        Long nextCursor = null;
        Long previousCursor = cursor;
        
        if (hasMore && !paginatedItems.isEmpty()) {
            nextCursor = paginatedItems.get(paginatedItems.size() - 1).getInternshipHeaderId();
        }

        return CursorPageResponse.<CompanyReviewsResponse.ReviewItem>builder()
                .result(paginatedItems)
                .meta(CursorPageResponse.Meta.builder()
                        .nextCursor(nextCursor)
                        .previousCursor(previousCursor)
                        .size(paginatedItems.size())
                        .hasMore(hasMore)
                        .build())
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
            if (!categoryService.existsSubCategoryById(subCategoryId)) {
                throw new ResourceNotFoundException("SubCategory not found with id: " + subCategoryId);
            }
        }
    }

    @Override
    public Map<Long, Double> getRatingsByCompanyIds(List<Long> companyIds) {
        if (companyIds == null || companyIds.isEmpty()) return Map.of();
        Map<Long, Double> result = new HashMap<>();
        for (CompanyRatingProjection row : internshipDetailRepository.findAverageRatingsByCompanyIds(companyIds)) {
            result.put(row.getCompanyId(), row.getAvgRating());
        }
        return result;
    }

    @Override
    public List<Long> getTop10CompanyIdsByRating() {
        return internshipDetailRepository.findTop10CompaniesByAverageRating().stream()
                .map(CompanyRatingProjection::getCompanyId)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, Long> getReviewCountsByCompanyIds(List<Long> companyIds) {
        if (companyIds == null || companyIds.isEmpty()) return Map.of();
        Map<Long, Long> result = new HashMap<>();
        for (CompanyReviewCountProjection row : internshipDetailRepository.findReviewCountsByCompanyIds(companyIds)) {
            result.put(row.getCompanyId(), row.getReviewCount());
        }
        return result;
    }

    @Override
    public RecentReviewResponse getRecentReviews() {
        List<RecentReviewProjection> results = internshipDetailRepository.findTop10RecentReviews();

        List<RecentReviewResponse.ReviewItem> items = results.stream()
                .map(row -> RecentReviewResponse.ReviewItem.builder()
                        .testimony(row.getTestimony())
                        .createdBy(row.getCreatedByName())
                        .averageRating(row.getAverageRating() != null ? row.getAverageRating() : 0.0)
                        .companyName(row.getCompanyName())
                        .companyCategory(row.getCompanyCategory())
                        .companyWebsite(row.getCompanyWebsite())
                        .jobTitle(row.getJobTitle())
                        .createdAt(OffsetDateTime.ofInstant(row.getCreatedAt(), ZoneId.systemDefault()))
                        .build())
                .collect(Collectors.toList());

        return RecentReviewResponse.builder()
                .items(items)
                .build();
    }
}
