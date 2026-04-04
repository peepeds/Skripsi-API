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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private static final String DURATION_UNIT = " months";
    private static final String DURATION_RANGE_SEPARATOR = " - ";
    private static final String LOOKUP_TYPE_INTERNSHIP_REVIEW = "INTERNSHIP_REVIEW";
    private static final String LOOKUP_CODE_RECRUITMENT_STEPS = "RECRUITMENT_STEPS";
    private static final String LOOKUP_CODE_INTERNSHIP_TYPE = "INTERNSHIP_TYPE";
    private static final String LOOKUP_CODE_SCHEME = "SCHEME";
    private static final String LOOKUP_CODE_ADMISSION_TRACK = "ADMISSION_TRACK";
    private static final String LOOKUP_CODE_RECRUITMENT_DURATION = "RECRUITMENT_DURATION";
    private static final int MAX_SUB_CATEGORIES = 3;

    private final InternshipHeaderRepository internshipHeaderRepository;
    private final InternshipDetailRepository internshipDetailRepository;
    private final RecruitmentStepRepository recruitmentStepRepository;
    private final InternshipJobSubCategoryRepository internshipJobSubCategoryRepository;
    private final ICompanyService companyService;
    private final SecurityUtils securityUtils;
    private final ICategoryService categoryService;
    private final AuditService auditService;
    private final LookupRepository lookupRepository;
    private final IUserService userService;
    private final SubCategoryRepository subCategoryRepository;

    public ReviewService(InternshipHeaderRepository internshipHeaderRepository,
                         InternshipDetailRepository internshipDetailRepository,
                         RecruitmentStepRepository recruitmentStepRepository,
                         InternshipJobSubCategoryRepository internshipJobSubCategoryRepository,
                         ICategoryService categoryService,
                         ICompanyService companyService,
                         SecurityUtils securityUtils,
                         AuditService auditService,
                         LookupRepository lookupRepository,
                         IUserService userService,
                         SubCategoryRepository subCategoryRepository) {
        this.internshipHeaderRepository = internshipHeaderRepository;
        this.internshipDetailRepository = internshipDetailRepository;
        this.recruitmentStepRepository = recruitmentStepRepository;
        this.internshipJobSubCategoryRepository = internshipJobSubCategoryRepository;
        this.categoryService = categoryService;
        this.companyService = companyService;
        this.securityUtils = securityUtils;
        this.auditService = auditService;
        this.lookupRepository = lookupRepository;
        this.userService = userService;
        this.subCategoryRepository = subCategoryRepository;
    }

    @Override
    public List<JobListItemResponse> searchJobOptions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        
        List<String> jobTitles = internshipHeaderRepository.searchJobTitles(query.trim());
        return jobTitles.stream()
                .map(this::toJobListItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewResponse createReview(Long companyId, CreateReviewRequest request, Long userId) {
        OffsetDateTime now = OffsetDateTime.now();

        validateUserNotAlreadyReviewedCompany(userId, companyId);
        validateTotalInternshipDuration(userId, request.getYear(), request.getDuration());
        validateLookupValues(request);
        validateAdditionalSubCategories(request.getSubCategoryIds());

        InternshipHeader savedHeader = internshipHeaderRepository.save(buildInternshipHeader(companyId, userId, request, now));
        InternshipDetail savedDetail = internshipDetailRepository.save(buildInternshipDetail(savedHeader.getInternshipHeaderId(), userId, request, now));

        saveRecruitmentSteps(savedHeader.getInternshipHeaderId(), request.getRecruitmentSteps());
        saveJobSubCategories(savedHeader.getInternshipHeaderId(), userId, request.getSubCategoryIds(), now);

        auditService.record(EntityTypeConstants.INTERNSHIP_REVIEW, savedHeader.getInternshipHeaderId(), ActionConstants.SUBMITTED, userId);

        return toSubmitResponse(savedHeader, savedDetail, now);
    }

    @Override
    public ReviewResponse createReviewBySlug(String slug, CreateReviewRequest request) {
        Long companyId = companyService.getCompanyIdBySlug(slug);
        Long userId = securityUtils.getCurrentUserId();
        
        return createReview(companyId, request, userId);
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

        return toSummaryResponse(type, duration, workSchemes, subCategories, ratings, recruitmentProcesses);
    }

    @Override
    public CursorPageResponse<CompanyReviewsResponse.ReviewItem> getCompanyReviews(String slug, String order, Long cursor, int limit) {
        log.info("[getCompanyReviews] slug={}, order={}, cursor={}, limit={}", slug, order, cursor, limit);
        Long companyId = companyService.getCompanyIdBySlug(slug);
        log.info("[getCompanyReviews] resolved companyId={}", companyId);

        Pageable pageable = PageRequest.of(0, limit + 1);
        boolean isLatest = !"oldest".equalsIgnoreCase(order);
        
        List<InternshipHeader> headers = isLatest
                ? internshipHeaderRepository.findPageByCompanyIdDesc(companyId, cursor, pageable)
                : internshipHeaderRepository.findPageByCompanyIdAsc(companyId, cursor, pageable);
        log.info("[getCompanyReviews] fetched {} headers from DB", headers.size());

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

        boolean hasMore = headers.size() > limit;
        List<InternshipHeader> pageHeaders = hasMore ? headers.subList(0, limit) : headers;

        ReviewDataBundle data = loadReviewData(pageHeaders);
        log.info("[getCompanyReviews] data bundle ready, building review items");

        List<CompanyReviewsResponse.ReviewItem> items = pageHeaders.stream()
                .map(header -> toReviewItem(header, data))
                .collect(Collectors.toList());

        Long nextCursor = hasMore
                ? pageHeaders.get(pageHeaders.size() - 1).getInternshipHeaderId()
                : null;

        log.info("[getCompanyReviews] returning {} items, hasMore={}", items.size(), hasMore);
        
        return CursorPageResponse.<CompanyReviewsResponse.ReviewItem>builder()
                .result(items)
                .meta(CursorPageResponse.Meta.builder()
                        .nextCursor(nextCursor)
                        .previousCursor(cursor)
                        .size(items.size())
                        .hasMore(hasMore)
                        .build())
                .build();
    }

    @Override
    public CursorPageResponse<RecruitmentProcessResponse.ProcessItem> getRecruitmentProcesses(String slug, Long cursor, int limit) {
        log.info("[getRecruitmentProcesses] slug={}, cursor={}, limit={}", slug, cursor, limit);
        Long companyId = companyService.getCompanyIdBySlug(slug);
        log.info("[getRecruitmentProcesses] resolved companyId={}", companyId);

        Pageable pageable = PageRequest.of(0, limit + 1);
        
        List<InternshipHeader> headers = internshipHeaderRepository.findPageByCompanyIdDesc(companyId, cursor, pageable);
        log.info("[getRecruitmentProcesses] fetched {} headers from DB", headers.size());

        if (headers.isEmpty()) {
            return CursorPageResponse.<RecruitmentProcessResponse.ProcessItem>builder()
                    .result(List.of())
                    .meta(CursorPageResponse.Meta.builder()
                            .nextCursor(null)
                            .previousCursor(null)
                            .size(0)
                            .hasMore(false)
                            .build())
                    .build();
        }

        boolean hasMore = headers.size() > limit;
        List<InternshipHeader> pageHeaders = hasMore ? headers.subList(0, limit) : headers;

        ReviewDataBundle data = loadReviewData(pageHeaders);
        log.info("[getRecruitmentProcesses] data bundle ready, building process items");

        List<RecruitmentProcessResponse.ProcessItem> items = pageHeaders.stream()
                .map(header -> toProcessItem(header, data))
                .collect(Collectors.toList());

        Long nextCursor = hasMore
                ? pageHeaders.get(pageHeaders.size() - 1).getInternshipHeaderId()
                : null;

        log.info("[getRecruitmentProcesses] returning {} items, hasMore={}", items.size(), hasMore);
        
        return CursorPageResponse.<RecruitmentProcessResponse.ProcessItem>builder()
                .result(items)
                .meta(CursorPageResponse.Meta.builder()
                        .nextCursor(nextCursor)
                        .previousCursor(cursor)
                        .size(items.size())
                        .hasMore(hasMore)
                        .build())
                .build();
    }

    @Override
    public RecruitmentProcessSummaryResponse getRecruitmentProcessSummary(String slug) {
        log.info("[getRecruitmentProcessSummary] slug={}", slug);
        Long companyId = companyService.getCompanyIdBySlug(slug);
        
        List<InternshipHeader> headers = internshipHeaderRepository.findByCompanyId(companyId);
        log.info("[getRecruitmentProcessSummary] found {} headers for companyId={}", headers.size(), companyId);
        
        if (headers.isEmpty()) {
            return RecruitmentProcessSummaryResponse.builder()
                    .difficulty(RecruitmentProcessSummaryResponse.Difficulty.builder()
                            .rating(0.0)
                            .count(0L)
                            .build())
                    .statistics(RecruitmentProcessSummaryResponse.Statistics.builder()
                            .admissionTrack(Map.of())
                            .recruitmentDuration("")
                            .frequentSelectionProcess(Map.of())
                            .build())
                    .build();
        }
        
        List<Long> headerIds = headers.stream()
                .map(InternshipHeader::getInternshipHeaderId)
                .collect(Collectors.toList());
        
        List<InternshipDetail> details = internshipDetailRepository.findByInternshipHeaderIds(headerIds);
        Map<String, Map<String, String>> lookupDescMap = loadInternshipReviewLookups();
        
        log.info("[getRecruitmentProcessSummary] found {} details from {} headers", details.size(), headers.size());
        
        Double avgDifficulty = details.stream()
                .mapToInt(d -> d.getInterviewDifficultyRating() != null ? d.getInterviewDifficultyRating() : 0)
                .filter(rating -> rating > 0)
                .average()
                .orElse(0.0);
        
        long totalRecords = details.size();
        
        Map<String, Long> admissionTrackCount = new HashMap<>();
        for (InternshipDetail detail : details) {
            if (detail.getAdmissionTrack() != null) {
                String trackValue = lookupDescMap.getOrDefault(LOOKUP_CODE_ADMISSION_TRACK, Map.of())
                        .get(detail.getAdmissionTrack());
                if (trackValue != null) {
                    admissionTrackCount.put(trackValue, admissionTrackCount.getOrDefault(trackValue, 0L) + 1);
                }
            }
        }
        
        Map<String, Long> recruitmentDurationCount = new HashMap<>();
        for (InternshipDetail detail : details) {
            if (detail.getRecruitmentDurationCode() != null) {
                String durationValue = lookupDescMap.getOrDefault(LOOKUP_CODE_RECRUITMENT_DURATION, Map.of())
                        .get(detail.getRecruitmentDurationCode());
                if (durationValue != null) {
                    recruitmentDurationCount.put(durationValue, recruitmentDurationCount.getOrDefault(durationValue, 0L) + 1);
                }
            }
        }
        
        List<RecruitmentStep> allSteps = recruitmentStepRepository.findByInternshipHeaderIdIn(headerIds);
        Map<String, Long> stepCount = new HashMap<>();
        for (RecruitmentStep step : allSteps) {
            String stepValue = lookupDescMap.getOrDefault(LOOKUP_CODE_RECRUITMENT_STEPS, Map.of())
                    .get(String.valueOf(step.getStepCode()));
            if (stepValue != null) {
                stepCount.put(stepValue, stepCount.getOrDefault(stepValue, 0L) + 1);
            }
        }
        
        Map<String, String> admissionTrackPercentages = admissionTrackCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.format("%.0f%%", (e.getValue() * 100.0) / totalRecords),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        
        String mostFrequentDuration = recruitmentDurationCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
        
        Map<String, String> frequentStepsPercentages = stepCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.format("%.0f%%", (e.getValue() * 100.0) / totalRecords),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        
        log.info("[getRecruitmentProcessSummary] calculated avgDifficulty={}, admissionTracks={}, durationSteps={}", 
                avgDifficulty, admissionTrackPercentages.size(), frequentStepsPercentages.size());
        
        return RecruitmentProcessSummaryResponse.builder()
                .difficulty(RecruitmentProcessSummaryResponse.Difficulty.builder()
                        .rating(Math.round(avgDifficulty * 10.0) / 10.0)
                        .count((long) details.size())
                        .build())
                .statistics(RecruitmentProcessSummaryResponse.Statistics.builder()
                        .admissionTrack(admissionTrackPercentages)
                        .recruitmentDuration(mostFrequentDuration)
                        .frequentSelectionProcess(frequentStepsPercentages)
                        .build())
                .build();
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
                .map(this::toRecentReviewItem)
                .collect(Collectors.toList());

        return toRecentReviewResponse(items);
    }

    private JobListItemResponse toJobListItemResponse(String jobTitle) {
        return JobListItemResponse.builder()
                .jobTitle(jobTitle)
                .build();
    }

    private void validateUserNotAlreadyReviewedCompany(Long userId, Long companyId) {
        var existingReview = internshipHeaderRepository.findByUserIdAndCompanyId(userId, companyId);
        if (existingReview.isPresent()) {
            throw new ValidationException("User has already submitted a review for this company");
        }
    }

    private void validateTotalInternshipDuration(Long userId, Integer year, Integer newDuration) {
        List<InternshipHeader> userReviewsInYear = internshipHeaderRepository.findByUserIdAndYear(userId, year);
        
        Integer totalDuration = userReviewsInYear.stream()
                .map(InternshipHeader::getDurationMonths)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        
        Integer totalWithNew = totalDuration + newDuration;
        
        if (totalWithNew > 12) {
            throw new ValidationException(
                String.format("Total internship duration in year %d exceeds 12 months. Current: %d months, New: %d months, Total: %d months",
                    year, totalDuration, newDuration, totalWithNew)
            );
        }
    }

    private void validateLookupValues(CreateReviewRequest request) {
        if (request.getInternshipType() != null && !request.getInternshipType().isBlank()) {
            validateLookupExists(LOOKUP_CODE_INTERNSHIP_TYPE, request.getInternshipType());
        }
        
        if (request.getWorkScheme() != null && !request.getWorkScheme().isBlank()) {
            validateLookupExists(LOOKUP_CODE_SCHEME, request.getWorkScheme());
        }
        
        if (request.getAdmissionTrack() != null && !request.getAdmissionTrack().isBlank()) {
            validateLookupExists(LOOKUP_CODE_ADMISSION_TRACK, request.getAdmissionTrack());
        }
        
        if (request.getRecruitmentDurationCode() != null && !request.getRecruitmentDurationCode().isBlank()) {
            validateLookupExists(LOOKUP_CODE_RECRUITMENT_DURATION, request.getRecruitmentDurationCode());
        }
        
        if (request.getRecruitmentSteps() != null && !request.getRecruitmentSteps().isEmpty()) {
            validateAllRecruitmentStepsExist(request.getRecruitmentSteps());
        }
        
        if (request.getSubCategoryIds() != null && !request.getSubCategoryIds().isEmpty()) {
            validateAllSubCategoriesExist(request.getSubCategoryIds());
        }
    }

    private void validateLookupExists(String lookupCode, String lookupValue) {
        var lookup = lookupRepository.findByLookupTypeAndLookupCode(LOOKUP_TYPE_INTERNSHIP_REVIEW, lookupCode);
        
        if (lookup.isEmpty()) {
            throw new ValidationException(String.format("Invalid lookup code: %s", lookupCode));
        }
    }

    private void validateAllRecruitmentStepsExist(List<Long> recruitmentStepIds) {
        if (recruitmentStepIds.isEmpty()) {
            return;
        }
        
        Set<Long> providedIds = new HashSet<>(recruitmentStepIds);
        List<Lookup> validSteps = lookupRepository.findByLookupType(LOOKUP_CODE_RECRUITMENT_STEPS);
        Set<Long> validIds = validSteps.stream()
                .map(step -> Long.parseLong(step.getLookupCode()))
                .collect(Collectors.toSet());
        
        Set<Long> invalidIds = providedIds.stream()
                .filter(id -> !validIds.contains(id))
                .collect(Collectors.toSet());
        
        if (!invalidIds.isEmpty()) {
            throw new ValidationException(
                String.format("Invalid recruitment step IDs: %s", invalidIds)
            );
        }
    }

    private void validateAllSubCategoriesExist(List<Long> subCategoryIds) {
        if (subCategoryIds.isEmpty()) {
            return;
        }
        
        List<SubCategory> foundSubCategories = subCategoryRepository.findBySubCategoryIds(subCategoryIds);
        Set<Long> foundIds = foundSubCategories.stream()
                .map(SubCategory::getSubCategoryId)
                .collect(Collectors.toSet());
        
        Set<Long> missingIds = subCategoryIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toSet());
        
        if (!missingIds.isEmpty()) {
            throw new ValidationException(
                String.format("SubCategory IDs not found: %s", missingIds)
            );
        }
    }

    private void validateAdditionalSubCategories(List<Long> subCategoryIds) {
        if (subCategoryIds == null || subCategoryIds.isEmpty()) {
            return;
        }

        if (subCategoryIds.size() > MAX_SUB_CATEGORIES) {
            throw new ValidationException("Maximum " + MAX_SUB_CATEGORIES + " additional subcategories allowed");
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

    private InternshipHeader buildInternshipHeader(Long companyId, Long userId, CreateReviewRequest request, OffsetDateTime now) {
        return InternshipHeader.builder()
                .userId(userId)
                .companyId(companyId)
                .jobTitle(request.getJobTitle())
                .startYear(LocalDate.of(request.getYear(), 1, 1))
                .durationMonths(request.getDuration())
                .createdAt(now)
                .createdBy(userId)
                .build();
    }

    private InternshipDetail buildInternshipDetail(Long headerId, Long userId, CreateReviewRequest request, OffsetDateTime now) {
        return InternshipDetail.builder()
                .internshipHeaderId(headerId)
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
                .admissionTrack(request.getAdmissionTrack())
                .recruitmentDurationCode(request.getRecruitmentDurationCode())
                .exampleQuestions(request.getExampleQuestions())
                .selectionProcess(request.getSelectionProcess())
                .tipsTricks(request.getTipsTricks())
                .createdAt(now)
                .createdBy(userId)
                .build();
    }

    private void saveRecruitmentSteps(Long headerId, List<Long> stepCodes) {
        if (stepCodes == null || stepCodes.isEmpty()) {
            return;
        }

        List<RecruitmentStep> steps = stepCodes.stream()
                .map(stepCode -> RecruitmentStep.builder()
                        .internshipHeaderId(headerId)
                        .stepCode(stepCode.intValue())
                        .build())
                .collect(Collectors.toList());
        recruitmentStepRepository.saveAll(steps);
    }

    private void saveJobSubCategories(Long headerId, Long userId, List<Long> subCategoryIds, OffsetDateTime now) {
        if (subCategoryIds == null || subCategoryIds.isEmpty()) {
            return;
        }

        List<InternshipJobSubCategory> subCategories = subCategoryIds.stream()
                .map(subCategoryId -> InternshipJobSubCategory.builder()
                        .internshipHeaderId(headerId)
                        .subCategoryId(subCategoryId)
                        .createdAt(now)
                        .createdBy(userId)
                        .build())
                .collect(Collectors.toList());
        internshipJobSubCategoryRepository.saveAll(subCategories);
    }

    private ReviewResponse toSubmitResponse(InternshipHeader savedHeader, InternshipDetail savedDetail, OffsetDateTime now) {
        return ReviewResponse.builder()
                .internshipHeaderId(savedHeader.getInternshipHeaderId())
                .internshipDetailId(savedDetail.getInternshipDetailId())
                .createdAt(now)
                .message(MessageConstants.Success.REVIEW_SUBMITTED_SUCCESSFULLY)
                .build();
    }

    private String getMostFrequentType(List<InternshipDetail> details) {
        return details.stream()
                .filter(d -> d.getType() != null)
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
                .filter(d -> d.getScheme() != null)
                .collect(Collectors.groupingBy(InternshipDetail::getScheme, Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(Map.Entry::getKey)
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

        Map<Long, String> subCategoryNameMap = categoryService.getSubCategoryNameMap(subCategoryIds);

        return subCategoryIds.stream()
                .map(subCategoryNameMap::get)
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

        Map<String, String> stepLookup = loadInternshipReviewLookups()
                .getOrDefault(LOOKUP_CODE_RECRUITMENT_STEPS, Map.of());

        List<String> stepDescriptions = steps.stream()
                .map(RecruitmentStep::getStepCode)
                .distinct()
                .map(code -> stepLookup.get(String.valueOf(code)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ReviewSummaryResponse.RecruitmentProcesses.builder()
                .rating(ratingAvg)
                .steps(stepDescriptions)
                .build();
    }

    private Map<String, Map<String, String>> loadInternshipReviewLookups() {
        List<Lookup> lookups = lookupRepository.findByLookupType(LOOKUP_TYPE_INTERNSHIP_REVIEW);
        
        log.info("[loadInternshipReviewLookups] raw lookup count={}", lookups.size());
        lookups.forEach(l -> log.debug("[loadInternshipReviewLookups] code={}, value={}, desc={}",
                l.getLookupCode(), l.getLookupValue(), l.getLookupDescription()));

        return lookups.stream()
                .filter(lookup -> lookup.getLookupDescription() != null)
                .collect(Collectors.groupingBy(
                        Lookup::getLookupCode,
                        Collectors.toMap(Lookup::getLookupValue, Lookup::getLookupDescription)
                ));
    }

    private ReviewSummaryResponse toSummaryResponse(
            String type, String duration, List<String> workSchemes, List<String> subCategories,
            ReviewSummaryResponse.Ratings ratings, ReviewSummaryResponse.RecruitmentProcesses recruitmentProcesses) {
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

    private ReviewDataBundle loadReviewData(List<InternshipHeader> headers) {
        log.info("[loadReviewData] loading data for headerCount={}", headers.size());
        List<Long> headerIds = headers.stream()
                .map(InternshipHeader::getInternshipHeaderId)
                .collect(Collectors.toList());

        List<InternshipDetail> details = internshipDetailRepository.findByInternshipHeaderIds(headerIds);
        
        log.info("[loadReviewData] found {} details", details.size());
        Map<Long, InternshipDetail> detailMap = details.stream()
                .collect(Collectors.toMap(InternshipDetail::getInternshipHeaderId, detail -> detail));

        List<InternshipJobSubCategory> jobSubCategories = internshipJobSubCategoryRepository.findByInternshipHeaderIdIn(headerIds);
        List<Long> allSubCategoryIds = jobSubCategories.stream()
                .map(InternshipJobSubCategory::getSubCategoryId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> subCategoryNameMap = categoryService.getSubCategoryNameMap(allSubCategoryIds);
        Map<Long, List<String>> subCategoriesMap = jobSubCategories.stream()
                .collect(Collectors.groupingBy(
                        InternshipJobSubCategory::getInternshipHeaderId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .map(jsc -> subCategoryNameMap.get(jsc.getSubCategoryId()))
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList())
                        )
                ));

        List<RecruitmentStep> allSteps = recruitmentStepRepository.findByInternshipHeaderIdIn(headerIds);
        Map<Long, List<Integer>> stepsMap = allSteps.stream()
                .collect(Collectors.groupingBy(
                        RecruitmentStep::getInternshipHeaderId,
                        Collectors.mapping(RecruitmentStep::getStepCode, Collectors.toList())
                ));

        log.info("[loadReviewData] loading lookups");
        Map<String, Map<String, String>> lookupDescMap = loadInternshipReviewLookups();
        log.info("[loadReviewData] lookup codes loaded: {}", lookupDescMap.keySet());

        List<Long> userIds = headers.stream()
                .map(InternshipHeader::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> userNameMap = userService.getUserNameMap(userIds);

        return new ReviewDataBundle(detailMap, subCategoriesMap, stepsMap, lookupDescMap, userNameMap);
    }

    private CompanyReviewsResponse.ReviewItem toReviewItem(InternshipHeader header, ReviewDataBundle data) {
        log.info("[toReviewItem] headerId={}, userId={}", header.getInternshipHeaderId(), header.getUserId());
        InternshipDetail detail = data.detailMap().get(header.getInternshipHeaderId());

        if (detail == null) {
            log.warn("[toReviewItem] no detail found for headerId={}", header.getInternshipHeaderId());
        } else {
            log.info("[toReviewItem] detailId={}, type={}, scheme={}, admissionTrack={}, recruitmentDurationCode={}",
                    detail.getInternshipDetailId(), detail.getType(), detail.getScheme(),
                    detail.getAdmissionTrack(), detail.getRecruitmentDurationCode());
        }

        Long headerId = header.getInternshipHeaderId();

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
                .internshipHeaderId(headerId)
                .internshipDetailId(detail != null ? detail.getInternshipDetailId() : null)
                .jobTitle(header.getJobTitle())
                .type(detail != null ? data.lookupDescMap().getOrDefault(LOOKUP_CODE_INTERNSHIP_TYPE, Map.of()).get(detail.getType()) : null)
                .workScheme(detail != null ? data.lookupDescMap().getOrDefault(LOOKUP_CODE_SCHEME, Map.of()).get(detail.getScheme()) : null)
                .admissionTrack(detail != null ? data.lookupDescMap().getOrDefault(LOOKUP_CODE_ADMISSION_TRACK, Map.of()).get(detail.getAdmissionTrack()) : null)
                .recruitmentDuration(detail != null ? data.lookupDescMap().getOrDefault(LOOKUP_CODE_RECRUITMENT_DURATION, Map.of()).get(detail.getRecruitmentDurationCode()) : null)
                .durationMonths(header.getDurationMonths())
                .year(year)
                .subCategories(data.subCategoriesMap().getOrDefault(headerId, List.of()))
                .ratings(ratings)
                .recruitmentSteps(data.stepsMap().getOrDefault(headerId, List.of()).stream()
                        .map(stepCode -> data.lookupDescMap().getOrDefault(LOOKUP_CODE_RECRUITMENT_STEPS, Map.of()).get(String.valueOf(stepCode)))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .interviewDifficulty(detail != null ? detail.getInterviewDifficultyRating() : null)
                .createdByName(data.userNameMap().get(header.getUserId()))
                .testimony(detail != null ? detail.getTestimony() : null)
                .pros(detail != null ? detail.getPros() : null)
                .cons(detail != null ? detail.getCons() : null)
                .exampleQuestions(detail != null ? detail.getExampleQuestions() : null)
                .selectionProcess(detail != null ? detail.getSelectionProcess() : null)
                .tipsTricks(detail != null ? detail.getTipsTricks() : null)
                .createdAt(detail != null ? detail.getCreatedAt() : null)
                .build();
    }

    private RecruitmentProcessResponse.ProcessItem toProcessItem(InternshipHeader header, ReviewDataBundle data) {
        log.info("[toProcessItem] headerId={}, userId={}", header.getInternshipHeaderId(), header.getUserId());
        InternshipDetail detail = data.detailMap().get(header.getInternshipHeaderId());

        if (detail == null) {
            log.warn("[toProcessItem] no detail found for headerId={}", header.getInternshipHeaderId());
        }

        Long headerId = header.getInternshipHeaderId();
        Integer year = header.getStartYear() != null ? header.getStartYear().getYear() : null;

        return RecruitmentProcessResponse.ProcessItem.builder()
                .internshipHeaderId(headerId)
                .internshipDetailId(detail != null ? detail.getInternshipDetailId() : null)
                .jobTitle(header.getJobTitle())
                .type(detail != null ? data.lookupDescMap().getOrDefault(LOOKUP_CODE_INTERNSHIP_TYPE, Map.of()).get(detail.getType()) : null)
                .workScheme(detail != null ? data.lookupDescMap().getOrDefault(LOOKUP_CODE_SCHEME, Map.of()).get(detail.getScheme()) : null)
                .admissionTrack(detail != null ? data.lookupDescMap().getOrDefault(LOOKUP_CODE_ADMISSION_TRACK, Map.of()).get(detail.getAdmissionTrack()) : null)
                .recruitmentDuration(detail != null ? data.lookupDescMap().getOrDefault(LOOKUP_CODE_RECRUITMENT_DURATION, Map.of()).get(detail.getRecruitmentDurationCode()) : null)
                .durationMonths(header.getDurationMonths())
                .year(year)
                .subCategories(data.subCategoriesMap().getOrDefault(headerId, List.of()))
                .recruitmentSteps(data.stepsMap().getOrDefault(headerId, List.of()).stream()
                        .map(stepCode -> data.lookupDescMap().getOrDefault(LOOKUP_CODE_RECRUITMENT_STEPS, Map.of()).get(String.valueOf(stepCode)))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .interviewDifficulty(detail != null ? detail.getInterviewDifficultyRating() : null)
                .createdByName(data.userNameMap().get(header.getUserId()))
                .exampleQuestions(detail != null ? detail.getExampleQuestions() : null)
                .selectionProcess(detail != null ? detail.getSelectionProcess() : null)
                .tipsTricks(detail != null ? detail.getTipsTricks() : null)
                .createdAt(detail != null ? detail.getCreatedAt() : null)
                .build();
    }

    private RecentReviewResponse.ReviewItem toRecentReviewItem(RecentReviewProjection row) {
        return RecentReviewResponse.ReviewItem.builder()
                .testimony(row.getTestimony())
                .createdBy(row.getCreatedByName())
                .averageRating(row.getAverageRating() != null ? row.getAverageRating() : 0.0)
                .companyName(row.getCompanyName())
                .companyCategory(row.getCompanyCategory())
                .companyWebsite(row.getCompanyWebsite())
                .jobTitle(row.getJobTitle())
                .createdAt(OffsetDateTime.ofInstant(row.getCreatedAt(), ZoneId.systemDefault()))
                .build();
    }

    private RecentReviewResponse toRecentReviewResponse(List<RecentReviewResponse.ReviewItem> items) {
        return RecentReviewResponse.builder()
                .items(items)
                .build();
    }

    private record ReviewDataBundle(
            Map<Long, InternshipDetail> detailMap,
            Map<Long, List<String>> subCategoriesMap,
            Map<Long, List<Integer>> stepsMap,
            Map<String, Map<String, String>> lookupDescMap,
            Map<Long, String> userNameMap
    ) {}
}
