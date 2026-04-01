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
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
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

@Service
@Transactional
public class CompanyService implements ICompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyRequestRepository companyRequestRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;
    private final IUserService userService;
    private final ICategoryService categoryService;
    private final IReviewService reviewService;
    private final NotificationHelper notificationHelper;

    public CompanyService(CompanyRepository companyRepository,
                          CompanyRequestRepository companyRequestRepository,
                          @Lazy AuditService auditService,
                          SecurityUtils securityUtils,
                          IUserService userService,
                          CompanyProfileRepository companyProfileRepository,
                          @Lazy ICategoryService categoryService,
                          @Lazy IReviewService reviewService,
                          NotificationHelper notificationHelper) {
        this.companyRepository = companyRepository;
        this.companyRequestRepository = companyRequestRepository;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
        this.userService = userService;
        this.companyProfileRepository = companyProfileRepository;
        this.categoryService = categoryService;
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

    @Override
    public CursorPageResponse<CompanyOptionsResponse> getCompany(Long cursor, int limit) {
        Pageable pageable = PageRequest.of(0, PaginationConstants.DEFAULT_PAGE_FETCH_SIZE);
        Page<Company> companies = companyRepository.findAll(pageable);

        List<Company> allCompanies = companies.getContent().stream()
                .filter(company -> cursor == null || company.getCompanyId() > cursor)
                .collect(Collectors.toList());

        List<Company> paginatedCompanies = allCompanies.stream()
                .limit(limit)
                .collect(Collectors.toList());

        boolean hasMore = allCompanies.size() > limit;

        List<Long> companyIds = paginatedCompanies.stream()
                .map(Company::getCompanyId)
                .collect(Collectors.toList());

        CompanyEnrichmentData enrichment = fetchEnrichmentData(companyIds);

        List<CompanyOptionsResponse> results = paginatedCompanies.stream()
                .map(company -> toOptionsResponse(company, enrichment))
                .collect(Collectors.toList());

        Long nextCursor = null;
        if (!results.isEmpty() && hasMore) {
            nextCursor = results.get(results.size() - 1).getCompanyId();
        }

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

        String abbreviation = request.getCompanyAbbreviation();
        if (abbreviation == null || abbreviation.trim().isEmpty()) {
            abbreviation = generateAbbreviation(companyName);
        }

        CompanyRequest companyRequest = CompanyRequest.builder()
                .companyName(companyName)
                .companyAbbreviation(abbreviation)
                .website(request.getWebsite())
                .isPartner(request.getIsPartner())
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

        return toRequestResponse(savedRequest);
    }

    @Override
    public CursorPageResponse<CompanyRequestResponse> getCompanyRequests(CompanyRequestStatus status, Long cursor, int limit) {
        Pageable pageable = PageRequest.of(0, PaginationConstants.DEFAULT_PAGE_FETCH_SIZE);
        Page<CompanyRequest> pageResult = (status == null
                ? companyRequestRepository.findAll(pageable)
                : companyRequestRepository.findByStatus(status, pageable));

        List<CompanyRequestResponse> allItems = pageResult.getContent().stream()
                .map(this::toRequestResponse)
                .collect(Collectors.toList());

        return buildCursorPageResponse(allItems, cursor, limit);
    }

    private CursorPageResponse<CompanyRequestResponse> buildCursorPageResponse(
            List<CompanyRequestResponse> allItems, Long cursor, int limit) {
        List<CompanyRequestResponse> filteredItems = filterItemsByCursor(allItems, cursor, limit);
        boolean hasMore = calculateHasMore(allItems, cursor, limit);
        Long nextCursor = calculateNextCursor(filteredItems, hasMore);

        return CursorPageResponse.<CompanyRequestResponse>builder()
                .result(filteredItems)
                .meta(CursorPageResponse.Meta.builder()
                        .nextCursor(nextCursor)
                        .previousCursor(cursor)
                        .size(filteredItems.size())
                        .hasMore(hasMore)
                        .build())
                .build();
    }

    private List<CompanyRequestResponse> filterItemsByCursor(
            List<CompanyRequestResponse> allItems, Long cursor, int limit) {
        return allItems.stream()
                .filter(item -> cursor == null || item.getCompanyRequestId() > cursor)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private boolean calculateHasMore(List<CompanyRequestResponse> allItems, Long cursor, int limit) {
        return allItems.stream()
                .filter(item -> cursor == null || item.getCompanyRequestId() > cursor)
                .count() > limit;
    }

    private Long calculateNextCursor(List<CompanyRequestResponse> filteredItems, boolean hasMore) {
        if (!filteredItems.isEmpty() && hasMore) {
            return filteredItems.get(filteredItems.size() - 1).getCompanyRequestId();
        }
        return null;
    }

    @Override
    public CompanyRequestResponse reviewCompanyRequest(Long requestId, ReviewCompanyRequestRequest request) {
        Long reviewerId = securityUtils.getCurrentUserId();

        CompanyRequest companyRequest = companyRequestRepository.findById(requestId)
                .orElseThrow(() -> new BadRequestExceptions(MessageConstants.NotFound.COMPANY_REQUEST_NOT_FOUND));

        validateCompanyRequestNotFinalized(companyRequest);

        if (request.getStatus() == CompanyRequestStatus.APPROVED) {
            createApprovedCompany(companyRequest, reviewerId);
        }

        updateCompanyRequestStatus(companyRequest, request, reviewerId);
        CompanyRequest savedRequest = companyRequestRepository.save(companyRequest);

        String actionLabel = getActionLabel(request.getStatus());
        updateNotifications(requestId, actionLabel, reviewerId, companyRequest);

        auditService.record(EntityTypeConstants.COMPANY_REQUEST, requestId, actionLabel, reviewerId, request.getReviewNote());

        return toRequestResponse(savedRequest);
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
                .bio("")
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

    @Override
    public CompanyRequestDetailResponse getCompanyRequestDetail(Long requestId) {
        Long currentUserId = securityUtils.getCurrentUserId();

        CompanyRequest companyRequest = companyRequestRepository.findById(requestId)
                .orElseThrow(() -> new BadRequestExceptions(MessageConstants.NotFound.COMPANY_REQUEST_NOT_FOUND));

        // Only the owner of the request may view its detail
        if (!companyRequest.getCreatedBy().equals(currentUserId)) {
            throw new CustomAccessDeniedException(MessageConstants.NotFound.ACCESS_DENIED_OWN_REQUESTS);
        }

        return toDetailResponse(companyRequest);
    }

    @Override
    public List<CompanyOptionsResponse> searchCompanies(String search) {
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

    public List<CompanyOptionsResponse> getTopCompaniesAvgRating() {
        List<Long> companyIds = reviewService.getTop10CompanyIdsByRating();

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
                .isPartner(profile != null ? profile.getIsPartner() : null)
                .subcategoryName(subcategoryName)
                .companySlug(company.getCompanySlug())
                .rating(enrichment.ratingMap.get(company.getCompanyId()))
                .totalReviews(totalReviews)
                .build();
    }

    private CompanyRequestResponse toRequestResponse(CompanyRequest companyRequest) {
        return CompanyRequestResponse.builder()
                .companyRequestId(companyRequest.getCompanyRequestId())
                .companyName(companyRequest.getCompanyName())
                .companyAbbreviation(companyRequest.getCompanyAbbreviation())
                .website(companyRequest.getWebsite())
                .isPartner(companyRequest.getIsPartner())
                .subcategoryId(companyRequest.getSubcategoryId())
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

    private String resolveUserName(Long userId) {
        return userService.resolveUserName(userId);
    }

    @Override
    public Long getCompanyIdBySlug(String slug) {
        Company company = companyRepository.findByCompanySlug(slug);
        if (company == null) {
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
        Pageable pageable = PageRequest.of(0, PaginationConstants.DEFAULT_PAGE_FETCH_SIZE);
        Page<CompanyOptionsResponse> pageResult = companyRepository.findCompaniesBySubCategoryId(subCategoryId, pageable);
        return buildCompanyOptionsCursorPage(pageResult.getContent(), cursor, limit);
    }

    @Override
    public CursorPageResponse<CompanyOptionsResponse> getCompaniesBySubCategoryIdViaProfile(Long subCategoryId, Long cursor, int limit) {
        Pageable pageable = PageRequest.of(0, PaginationConstants.DEFAULT_PAGE_FETCH_SIZE);
        Page<CompanyOptionsResponse> pageResult = companyRepository.findCompaniesBySubCategoryIdViaProfile(subCategoryId, pageable);
        return buildCompanyOptionsCursorPage(pageResult.getContent(), cursor, limit);
    }

    @Override
    public CursorPageResponse<CompanyOptionsResponse> getCompaniesBySubCategoryNameViaProfile(String subCategoryName, Long cursor, int limit) {
        Pageable pageable = PageRequest.of(0, PaginationConstants.DEFAULT_PAGE_FETCH_SIZE);
        Page<CompanyOptionsResponse> pageResult = companyRepository.findCompaniesBySubCategoryNameViaProfile(subCategoryName, pageable);
        return buildCompanyOptionsCursorPage(pageResult.getContent(), cursor, limit);
    }

    private CursorPageResponse<CompanyOptionsResponse> buildCompanyOptionsCursorPage(
            List<CompanyOptionsResponse> allItems, Long cursor, int limit) {
        List<CompanyOptionsResponse> filteredItems = allItems.stream()
                .filter(item -> cursor == null || item.getCompanyId() > cursor)
                .limit(limit)
                .collect(Collectors.toList());

        boolean hasMore = allItems.stream()
                .filter(item -> cursor == null || item.getCompanyId() > cursor)
                .count() > limit;

        Long nextCursor = (!filteredItems.isEmpty() && hasMore)
                ? filteredItems.get(filteredItems.size() - 1).getCompanyId()
                : null;

        return CursorPageResponse.<CompanyOptionsResponse>builder()
                .result(filteredItems)
                .meta(CursorPageResponse.Meta.builder()
                        .nextCursor(nextCursor)
                        .previousCursor(cursor)
                        .size(filteredItems.size())
                        .hasMore(hasMore)
                        .build())
                .build();
    }

    @Override
    public CompanyOptionsResponse getCompanyBySlug(String slug) {
        Company company = companyRepository.findByCompanySlug(slug);
        if (company == null) {
            throw new BadRequestExceptions("Company not found");
        }

        CompanyProfile profile = companyProfileRepository.findByCompanyId(company.getCompanyId());
        if (profile == null) {
            throw new BadRequestExceptions("Company profile not found");
        }

        CompanyEnrichmentData enrichment = fetchEnrichmentData(Collections.singletonList(company.getCompanyId()));

        String subcategoryName = profile.getSubcategoryId() != null
                ? enrichment.subcategoryNameMap.get(profile.getSubcategoryId())
                : null;

        return CompanyOptionsResponse.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .companyAbbreviation(company.getCompanyAbbreviation())
                .website(profile.getWebsite())
                .isPartner(profile.getIsPartner())
                .subcategoryName(subcategoryName)
                .companySlug(company.getCompanySlug())
                .rating(enrichment.ratingMap.get(company.getCompanyId()))
                .totalReviews(enrichment.reviewCountMap.getOrDefault(company.getCompanyId(), 0L))
                .build();
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

    @Override
    public Boolean isCompanyRequestOwner(Long requestId, Long userId) {
        if (requestId == null || userId == null) return false;
        return companyRequestRepository.findById(requestId)
                .map(req -> req.getCreatedBy().equals(userId))
                .orElse(false);
    }
}
