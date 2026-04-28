package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.*;
import com.example.skripsi.models.company.*;
import com.example.skripsi.models.constant.*;
import com.example.skripsi.repositories.*;
import com.example.skripsi.securities.*;
import com.example.skripsi.utilities.NotificationHelper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class CompanyService implements ICompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyRequestRepository companyRequestRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final CompanySaveRepository companySaveRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;
    private final IUserService userService;
    private final ICategoryService categoryService;
    private final IReviewService reviewService;
    private final NotificationHelper notificationHelper;
    private final UserProfileRepository userProfileRepository;

    public CompanyService(CompanyRepository companyRepository,
                          CompanyRequestRepository companyRequestRepository,
                          @Lazy AuditService auditService,
                          SecurityUtils securityUtils,
                          IUserService userService,
                          CompanyProfileRepository companyProfileRepository,
                          CompanySaveRepository companySaveRepository,
                          @Lazy ICategoryService categoryService,
                          @Lazy IReviewService reviewService,
                          NotificationHelper notificationHelper,
                          UserProfileRepository userProfileRepository) {
        this.companyRepository = companyRepository;
        this.companyRequestRepository = companyRequestRepository;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
        this.userService = userService;
        this.companyProfileRepository = companyProfileRepository;
        this.companySaveRepository = companySaveRepository;
        this.categoryService = categoryService;
        this.userProfileRepository = userProfileRepository;
        this.reviewService = reviewService;
        this.notificationHelper = notificationHelper;
    }

    private static class CompanyEnrichmentData {
        final Map<Long, Double> ratingMap;
        final Map<Long, Long> reviewCountMap;
        final Map<Long, CompanyProfile> profileMap;
        final Map<Long, String> subcategoryNameMap;

        CompanyEnrichmentData(Map<Long, Double> ratingMap,
                              Map<Long, Long> reviewCountMap,
                              Map<Long, CompanyProfile> profileMap,
                              Map<Long, String> subcategoryNameMap) {
            this.ratingMap = ratingMap;
            this.reviewCountMap = reviewCountMap;
            this.profileMap = profileMap;
            this.subcategoryNameMap = subcategoryNameMap;
        }
    }

    @Override
    public CursorPageResponse<CompanyOptionsResponse> getCompany(Long cursor, int limit, String sort) {
        log.info("[getCompany] cursor={} limit={} sort={}", cursor, limit, sort);
        Pageable pageable = PageRequest.of(0, limit + 1);

        List<Company> companies;
        if ("latest".equals(sort)) {
            companies = companyRepository.findPageFromCursorLatest(cursor, pageable);
        } else if ("top".equals(sort)) {
            companies = companyRepository.findPageFromCursorTop(cursor, pageable);
        } else {
            companies = companyRepository.findPageFromCursor(cursor, pageable);
        }

        boolean hasMore = companies.size() > limit;
        List<Company> pageCompanies = hasMore ? companies.subList(0, limit) : companies;

        List<Long> companyIds = pageCompanies.stream()
                .map(Company::getCompanyId)
                .collect(Collectors.toList());

        CompanyEnrichmentData enrichment = fetchEnrichmentData(companyIds);

        List<CompanyOptionsResponse> results = pageCompanies.stream()
                .map(company -> toOptionsResponse(company, enrichment))
                .collect(Collectors.toList());

        Long nextCursor = hasMore && !results.isEmpty()
                ? results.get(results.size() - 1).getCompanyId()
                : null;

        return CursorPageResponse.<CompanyOptionsResponse>builder()
                .result(results)
                .meta(CursorPageResponse.Meta.builder()
                        .nextCursor(nextCursor)
                        .previousCursor(cursor)
                        .size(results.size())
                        .hasMore(hasMore)
                        .build())
                .build();
    }

    @Override
    public CompanyRequestResponse submitCompanyRequest(CreateCompanyRequestRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        String companyName = request.getCompanyName().trim();
        log.info("[submitCompanyRequest] userId={} companyName={}", userId, companyName);

        String abbreviation = request.getCompanyAbbreviation();

        if (abbreviation == null || abbreviation.trim().isEmpty()) {
            abbreviation = generateAbbreviation(companyName);
        }

        CompanyRequest companyRequest = CompanyRequest.builder()
                .companyName(companyName)
                .companyAbbreviation(abbreviation)
                .website(request.getWebsite())
                .bio(request.getBio())
                .isPartner(request.getIsPartner())
                .subcategoryId(request.getSubcategoryId())
                .status(CompanyRequestStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .createdBy(userId)
                .build();
        CompanyRequest savedRequest = companyRequestRepository.save(companyRequest);

        notificationHelper.createNotificationWithUserNotification(
                EntityTypeConstants.COMPANY_REQUEST,
                ActionConstants.SUBMITTED,
                savedRequest.getCompanyRequestId(),
                userId,
                userId);

        auditService.record(EntityTypeConstants.COMPANY_REQUEST, savedRequest.getCompanyRequestId(), ActionConstants.SUBMITTED, userId);
        log.info("[submitCompanyRequest] created requestId={} userId={}", savedRequest.getCompanyRequestId(), userId);

        return toRequestResponse(savedRequest);
    }

    @Override
    public CursorPageResponse<CompanyRequestResponse> getCompanyRequests(CompanyRequestStatus status, Long cursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<CompanyRequest> requests = status == null
                ? companyRequestRepository.findPageFromCursor(cursor, pageable)
                : companyRequestRepository.findPageByStatusFromCursor(status, cursor, pageable);

        boolean hasMore = requests.size() > limit;
        List<CompanyRequest> pageRequests = hasMore ? requests.subList(0, limit) : requests;

        List<CompanyRequestResponse> items = pageRequests.stream()
                .map(this::toRequestResponse)
                .collect(Collectors.toList());

        Long nextCursor = hasMore && !items.isEmpty()
                ? items.get(items.size() - 1).getCompanyRequestId()
                : null;

        return CursorPageResponse.<CompanyRequestResponse>builder()
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
    public CompanyRequestResponse reviewCompanyRequest(Long requestId, ReviewCompanyRequestRequest request) {
        Long reviewerId = securityUtils.getCurrentUserId();
        log.info("[reviewCompanyRequest] requestId={} status={} reviewerId={}", requestId, request.getStatus(), reviewerId);

        CompanyRequest companyRequest = companyRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("[reviewCompanyRequest] request not found requestId={}", requestId);
                    return new BadRequestExceptions(MessageConstants.NotFound.COMPANY_REQUEST_NOT_FOUND);
                });

        validateCompanyRequestNotFinalized(companyRequest);

        if (request.getStatus() == CompanyRequestStatus.APPROVED) {
            createApprovedCompany(companyRequest, reviewerId);
        }

        updateCompanyRequestStatus(companyRequest, request, reviewerId);
        CompanyRequest savedRequest = companyRequestRepository.save(companyRequest);

        String actionLabel = getActionLabel(request.getStatus());
        updateNotifications(requestId, actionLabel, reviewerId, companyRequest);

        auditService.record(EntityTypeConstants.COMPANY_REQUEST, requestId, actionLabel, reviewerId, request.getReviewNote());
        log.info("[reviewCompanyRequest] done requestId={} action={} reviewerId={}", requestId, actionLabel, reviewerId);

        return toRequestResponse(savedRequest);
    }

    @Override
    public CompanyRequestDetailResponse getCompanyRequestDetail(Long requestId) {
        Long currentUserId = securityUtils.getCurrentUserId();

        CompanyRequest companyRequest = companyRequestRepository.findById(requestId)
                .orElseThrow(() -> new BadRequestExceptions(MessageConstants.NotFound.COMPANY_REQUEST_NOT_FOUND));

        if (!companyRequest.getCreatedBy().equals(currentUserId)) {
            throw new CustomAccessDeniedException(MessageConstants.NotFound.ACCESS_DENIED_OWN_REQUESTS);
        }

        return toDetailResponse(companyRequest);
    }

    @Override
    public List<CompanyOptionsResponse> searchCompanies(String search) {
        log.info("[searchCompanies] search={}", search);
        List<CompanyOptionsResponse> searchResults = companyRepository.searchCompanies(search);

        if (searchResults.isEmpty()) {
            return searchResults;
        }

        List<Long> companyIds = searchResults.stream()
                .map(CompanyOptionsResponse::getCompanyId)
                .collect(Collectors.toList());

        CompanyEnrichmentData enrichment = fetchEnrichmentData(companyIds);

        Map<Long, Company> companyMap = companyRepository.findAllById(companyIds).stream()
                .collect(Collectors.toMap(Company::getCompanyId, company -> company));

        return searchResults.stream()
                .map(result -> {
                    Company company = companyMap.get(result.getCompanyId());
                    return company != null ? toOptionsResponse(company, enrichment) : result;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CompanyOptionsResponse> getTopCompaniesAvgRating(Long userId) {
        List<Long> companyIds = resolveTopCompanyIds(userId);

        if (companyIds.isEmpty()) {
            return List.of();
        }

        CompanyEnrichmentData enrichment = fetchEnrichmentData(companyIds);

        Map<Long, Company> companyMap = companyRepository.findAllById(companyIds).stream()
                .collect(Collectors.toMap(Company::getCompanyId, company -> company));

        return companyIds.stream()
                .map(companyMap::get)
                .filter(Objects::nonNull)
                .map(company -> toOptionsResponse(company, enrichment))
                .collect(Collectors.toList());
    }

    private List<Long> resolveTopCompanyIds(Long userId) {
        if (userId == null) {
            return reviewService.getTop10CompanyIdsByRating();
        }

        Long majorId = userProfileRepository.findByUserUserId(userId)
                .map(profile -> profile.getMajor().getMajorId().longValue())
                .orElse(null);

        if (majorId == null) {
            return reviewService.getTop10CompanyIdsByRating();
        }

        List<Long> majorFiltered = reviewService.getTop10CompanyIdsByRatingForMajor(majorId);
        List<Long> global = reviewService.getTop10CompanyIdsByRating();

        List<Long> merged = new java.util.ArrayList<>(majorFiltered);
        for (Long id : global) {
            if (!majorFiltered.contains(id)) {
                merged.add(id);
                if (merged.size() == 10) break;
            }
        }
        return merged;
    }

    @Override
    public Long getCompanyIdBySlug(String slug) {
        Company company = companyRepository.findByCompanySlug(slug);

        if (company == null) {
            log.warn("[getCompanyIdBySlug] company not found slug={}", slug);
            throw new ResourceNotFoundException("Company with slug '" + slug + "' not found");
        }

        return company.getCompanyId();
    }

    @Override
    public String getCompanyRequestName(Long requestId) {
        return companyRequestRepository.findById(requestId)
                .map(CompanyRequest::getCompanyName)
                .orElse("Unknown");
    }

    @Override
    public CursorPageResponse<CompanyOptionsResponse> getCompaniesBySubCategoryId(Long subCategoryId, Long cursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<CompanyOptionsResponse> companies = companyRepository.findCompaniesBySubCategoryIdFromCursor(subCategoryId, cursor, pageable);
        return buildCompanyOptionsCursorPage(companies, cursor, limit);
    }

    @Override
    public CursorPageResponse<CompanyOptionsResponse> getCompaniesBySubCategoryIdViaProfile(Long subCategoryId, Long cursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<CompanyOptionsResponse> companies = companyRepository.findCompaniesBySubCategoryIdViaProfileFromCursor(subCategoryId, cursor, pageable);
        return buildCompanyOptionsCursorPage(companies, cursor, limit);
    }

    @Override
    public CursorPageResponse<CompanyOptionsResponse> getCompaniesBySubCategoryNameViaProfile(String subCategoryName, Long cursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<CompanyOptionsResponse> companies = companyRepository.findCompaniesBySubCategoryNameViaProfileFromCursor(subCategoryName, cursor, pageable);
        return buildCompanyOptionsCursorPage(companies, cursor, limit);
    }

    @Override
    public CompanyOptionsResponse getCompanyBySlug(String slug) {
        log.info("[getCompanyBySlug] slug={}", slug);
        Company company = companyRepository.findByCompanySlug(slug);

        if (company == null) {
            log.warn("[getCompanyBySlug] company not found slug={}", slug);
            throw new BadRequestExceptions("Company not found");
        }

        CompanyEnrichmentData enrichment = fetchEnrichmentData(Collections.singletonList(company.getCompanyId()));

        if (enrichment.profileMap.get(company.getCompanyId()) == null) {
            log.warn("[getCompanyBySlug] profile not found companyId={}", company.getCompanyId());
            throw new BadRequestExceptions("Company profile not found");
        }

        return toOptionsResponse(company, enrichment);
    }

    @Override
    public CursorPageResponse<CompanyOptionsResponse> getMyBookmarks(Long cursor, int limit) {
        Long userId = securityUtils.getCurrentUserId();

        Pageable pageable = PageRequest.of(0, limit + 1);
        List<CompanySave> saves = companySaveRepository.findBookmarksByUserIdFromCursor(userId, cursor, pageable);

        boolean hasMore = saves.size() > limit;
        List<CompanySave> page = hasMore ? saves.subList(0, limit) : saves;

        List<Long> companyIds = page.stream().map(CompanySave::getCompanyId).collect(Collectors.toList());
        List<Company> companies = companyRepository.findAllById(companyIds);
        Map<Long, Company> companyMap = companies.stream()
                .collect(Collectors.toMap(Company::getCompanyId, c -> c));

        CompanyEnrichmentData enrichment = fetchEnrichmentData(companyIds);

        List<CompanyOptionsResponse> items = page.stream()
                .filter(s -> companyMap.containsKey(s.getCompanyId()))
                .map(s -> toOptionsResponse(companyMap.get(s.getCompanyId()), enrichment))
                .collect(Collectors.toList());

        Long nextCursor = hasMore && !page.isEmpty()
                ? page.get(page.size() - 1).getCompanySaveId()
                : null;

        return CursorPageResponse.<CompanyOptionsResponse>builder()
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
    public Map<Long, Company> getCompanyInfoByIds(List<Long> companyIds) {
        return companyRepository.findAllById(companyIds).stream()
                .collect(Collectors.toMap(Company::getCompanyId, c -> c));
    }

    @Override
    public Boolean isCompanyRequestOwner(Long requestId, Long userId) {
        return companyRequestRepository.findById(requestId)
                .map(req -> req.getCreatedBy().equals(userId))
                .orElse(false);
    }

    @Override
    public Boolean saveCompany(String slug, SaveCompanyRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        Long companyId = getCompanyIdBySlug(slug);
        log.info("[saveCompany] userId={} companyId={} isSave={}", userId, companyId, request.getIsSave());

        CompanySave save = companySaveRepository.findByUserIdAndCompanyId(userId, companyId)
                .orElse(CompanySave.builder()
                        .userId(userId)
                        .companyId(companyId)
                        .createdAt(OffsetDateTime.now())
                        .build());

        save.setIsSave(request.getIsSave());
        save.setUpdatedAt(OffsetDateTime.now());
        companySaveRepository.save(save);

        return request.getIsSave();
    }

    private CompanyEnrichmentData fetchEnrichmentData(List<Long> companyIds) {
        if (companyIds.isEmpty()) {
            return new CompanyEnrichmentData(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
        }

        Map<Long, Double> ratingMap = reviewService.getRatingsByCompanyIds(companyIds);
        Map<Long, Long> reviewCountMap = reviewService.getReviewCountsByCompanyIds(companyIds);

        List<CompanyProfile> profiles = companyProfileRepository.findByCompanyIds(companyIds);
        Map<Long, CompanyProfile> profileMap = new HashMap<>();

        for (CompanyProfile profile : profiles) {
            profileMap.put(profile.getCompanyId(), profile);
        }

        List<Long> subcategoryIds = profiles.stream()
                .map(CompanyProfile::getSubcategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Long, String> subcategoryNameMap = categoryService.getSubCategoryNameMap(subcategoryIds);

        return new CompanyEnrichmentData(ratingMap, reviewCountMap, profileMap, subcategoryNameMap);
    }

    private void validateCompanyRequestNotFinalized(CompanyRequest companyRequest) {
        if (companyRequest.getStatus() == CompanyRequestStatus.APPROVED
                || companyRequest.getStatus() == CompanyRequestStatus.REJECTED) {
            throw new BadRequestExceptions(MessageConstants.Company.COMPANY_REQUEST_ALREADY_FINALIZED
                    + companyRequest.getStatus().name().toLowerCase() + MessageConstants.Company.AND_CANNOT_BE_CHANGED);
        }
    }

    private void createApprovedCompany(CompanyRequest companyRequest, Long reviewerId) {
        Company savedCompany = companyRepository.save(Company.builder()
                .companyName(companyRequest.getCompanyName())
                .companyAbbreviation(companyRequest.getCompanyAbbreviation())
                .createdBy(reviewerId)
                .build());
        companyProfileRepository.save(CompanyProfile.builder()
                .companyId(savedCompany.getCompanyId())
                .website(companyRequest.getWebsite())
                .bio(companyRequest.getBio())
                .isPartner(false)
                .subcategoryId(companyRequest.getSubcategoryId())
                .createdAt(OffsetDateTime.now())
                .createdBy(reviewerId)
                .build());
    }

    private void updateCompanyRequestStatus(CompanyRequest companyRequest,
                                            ReviewCompanyRequestRequest request, Long reviewerId) {
        companyRequest.setStatus(request.getStatus());
        companyRequest.setReviewNote(request.getReviewNote());
        companyRequest.setReviewedAt(OffsetDateTime.now());
        companyRequest.setReviewedBy(reviewerId);
        companyRequest.setUpdatedAt(OffsetDateTime.now());
        companyRequest.setUpdatedBy(reviewerId);
    }

    private String getActionLabel(CompanyRequestStatus status) {
        return status == CompanyRequestStatus.APPROVED ? ActionConstants.APPROVED : ActionConstants.REJECTED;
    }

    private void updateNotifications(Long requestId, String actionLabel, Long reviewerId, CompanyRequest companyRequest) {
        List<Notification> existingNotifications = userService.findNotificationsByTypeAndReferenceId(EntityTypeConstants.COMPANY_REQUEST, requestId);

        if (existingNotifications.isEmpty()) {
            throw new BadRequestExceptions(MessageConstants.NotFound.NOTIFICATION_NOT_FOUND);
        }

        Notification existingNotification = existingNotifications.get(0);
        notificationHelper.updateNotificationAction(existingNotification, actionLabel, reviewerId);
        notificationHelper.updateOrCreateUserNotification(existingNotification, companyRequest.getCreatedBy());
    }

    private String resolveUserName(Long userId) {
        return userService.resolveUserName(userId);
    }

    private String generateAbbreviation(String companyName) {
        String[] words = companyName.split("\\s+");
        StringBuilder abbreviationBuilder = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                abbreviationBuilder.append(Character.toUpperCase(word.charAt(0)));
            }
        }

        return abbreviationBuilder.toString();
    }

    private CursorPageResponse<CompanyOptionsResponse> buildCompanyOptionsCursorPage(
            List<CompanyOptionsResponse> companies, Long cursor, int limit) {
        boolean hasMore = companies.size() > limit;
        List<CompanyOptionsResponse> items = hasMore ? companies.subList(0, limit) : companies;

        Long nextCursor = hasMore && !items.isEmpty()
                ? items.get(items.size() - 1).getCompanyId()
                : null;

        return CursorPageResponse.<CompanyOptionsResponse>builder()
                .result(items)
                .meta(CursorPageResponse.Meta.builder()
                        .nextCursor(nextCursor)
                        .previousCursor(cursor)
                        .size(items.size())
                        .hasMore(hasMore)
                        .build())
                .build();
    }

    private CompanyOptionsResponse toOptionsResponse(Company company, CompanyEnrichmentData enrichment) {
        CompanyProfile profile = enrichment.profileMap.get(company.getCompanyId());
        String subcategoryName = (profile != null && profile.getSubcategoryId() != null)
                ? enrichment.subcategoryNameMap.get(profile.getSubcategoryId())
                : null;

        Long totalReviews = enrichment.reviewCountMap.getOrDefault(company.getCompanyId(), 0L);

        return CompanyOptionsResponse.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .companyAbbreviation(company.getCompanyAbbreviation())
                .website(profile != null ? profile.getWebsite() : null)
                .bio(profile != null ? profile.getBio() : null)
                .isPartner(profile != null ? profile.getIsPartner() : null)
                .subcategoryName(subcategoryName)
                .companySlug(company.getCompanySlug())
                .rating(enrichment.ratingMap.get(company.getCompanyId()))
                .totalReviews(totalReviews)
                .build();
    }

    private CompanyRequestResponse toRequestResponse(CompanyRequest companyRequest) {
        String subcategoryName = null;
        if (companyRequest.getSubcategoryId() != null) {
            Map<Long, String> nameMap = categoryService.getSubCategoryNameMap(List.of(companyRequest.getSubcategoryId()));
            subcategoryName = nameMap.get(companyRequest.getSubcategoryId());
        }
        return CompanyRequestResponse.builder()
                .companyRequestId(companyRequest.getCompanyRequestId())
                .companyName(companyRequest.getCompanyName())
                .companyAbbreviation(companyRequest.getCompanyAbbreviation())
                .website(companyRequest.getWebsite())
                .bio(companyRequest.getBio())
                .isPartner(companyRequest.getIsPartner())
                .subcategoryName(subcategoryName)
                .status(companyRequest.getStatus())
                .createdAt(companyRequest.getCreatedAt())
                .createdBy(resolveUserName(companyRequest.getCreatedBy()))
                .reviewedAt(companyRequest.getReviewedAt())
                .reviewedBy(resolveUserName(companyRequest.getReviewedBy()))
                .reviewNote(companyRequest.getReviewNote())
                .build();
    }

    private CompanyRequestDetailResponse toDetailResponse(CompanyRequest companyRequest) {
        String submittedBy = resolveUserName(companyRequest.getCreatedBy());
        String reviewedBy = companyRequest.getReviewedBy() != null
                ? resolveUserName(companyRequest.getReviewedBy()) : null;

        List<CompanyRequestDetailResponse.DocumentResponse> documents = userService
                .findNotificationsByTypeAndReferenceId(EntityTypeConstants.COMPANY_REQUEST, companyRequest.getCompanyRequestId())
                .stream()
                .flatMap(notif -> userService.findUserCertificateRequestsByNotificationId(notif.getNotificationId()).stream())
                .map(d -> CompanyRequestDetailResponse.DocumentResponse.builder()
                        .fileName(d.getDocumentName())
                        .url(d.getDocumentUrl())
                        .build())
                .collect(Collectors.toList());

        return CompanyRequestDetailResponse.builder()
                .requestDetails(CompanyRequestDetailResponse.RequestDetails.builder()
                        .id(companyRequest.getCompanyRequestId())
                        .companyName(companyRequest.getCompanyName())
                        .companyAbbreviation(companyRequest.getCompanyAbbreviation())
                        .website(companyRequest.getWebsite())
                        .bio(companyRequest.getBio())
                        .subcategoryId(companyRequest.getSubcategoryId())
                        .submittedBy(submittedBy)
                        .submittedAt(companyRequest.getCreatedAt())
                        .documents(documents)
                        .build())
                .reviewInformation(CompanyRequestDetailResponse.ReviewInformation.builder()
                        .status(companyRequest.getStatus())
                        .reviewNote(companyRequest.getReviewNote())
                        .reviewedBy(reviewedBy)
                        .reviewedAt(companyRequest.getReviewedAt())
                        .build())
                .build();
    }
}
