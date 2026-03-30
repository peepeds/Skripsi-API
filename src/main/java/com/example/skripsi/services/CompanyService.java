package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.*;
import com.example.skripsi.models.company.*;
import com.example.skripsi.repositories.*;
import com.example.skripsi.securities.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class CompanyService implements ICompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyRequestRepository companyRequestRepository;
    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final UserCertificateRequestRepository documentRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final InternshipDetailRepository internshipDetailRepository;

    public CompanyService(CompanyRepository companyRepository,
                          CompanyRequestRepository companyRequestRepository,
                          NotificationRepository notificationRepository,
                          UserNotificationRepository userNotificationRepository,
                          UserCertificateRequestRepository documentRepository,
                          AuditService auditService,
                          SecurityUtils securityUtils,
                          UserRepository userRepository,
                          CompanyProfileRepository companyProfileRepository,
                          SubCategoryRepository subCategoryRepository,
                          InternshipDetailRepository internshipDetailRepository) {
        this.companyRepository = companyRepository;
        this.companyRequestRepository = companyRequestRepository;
        this.notificationRepository = notificationRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.documentRepository = documentRepository;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
        this.userRepository = userRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.internshipDetailRepository = internshipDetailRepository;
    }

    @Override
    public PageResponse<CompanyOptionsResponse> getCompany(int page, int limit) {
        final int MAX_TOTAL_ELEMENTS = 1000;
        int requestedOffset = page * limit;
        if (requestedOffset >= MAX_TOTAL_ELEMENTS) {
            throw new BadRequestExceptions("limit exceeded");
        }
        Pageable pageable = PageRequest.of(page, limit);
        Page<Company> companies = companyRepository.findAll(pageable);

        List<Long> companyIds = companies.getContent().stream()
                .map(Company::getCompanyId)
                .collect(Collectors.toList());

        final java.util.Map<Long, Double> ratingMap = new java.util.HashMap<>();
        final java.util.Map<Long, Long> reviewCountMap = new java.util.HashMap<>();
        final java.util.Map<Long, CompanyProfile> profileMap = new java.util.HashMap<>();
        final java.util.Map<Long, SubCategory> subcategoryMap = new java.util.HashMap<>();

        if (!companyIds.isEmpty()) {
            List<Object[]> ratings = internshipDetailRepository.findAverageRatingsByCompanyIds(companyIds);
            for (Object[] row : ratings) {
                Long companyId = ((Number) row[0]).longValue();
                Double avgRating = row[1] != null ? ((Number) row[1]).doubleValue() : null;
                ratingMap.put(companyId, avgRating);
            }

            List<Object[]> reviewCountData = internshipDetailRepository.findReviewCountsByCompanyIds(companyIds);
            for (Object[] row : reviewCountData) {
                Long companyId = ((Number) row[0]).longValue();
                Long reviewCount = ((Number) row[1]).longValue();
                reviewCountMap.put(companyId, reviewCount);
            }

            List<CompanyProfile> profiles = companyProfileRepository.findByCompanyIds(companyIds);
            for (CompanyProfile profile : profiles) {
                profileMap.put(profile.getCompanyId(), profile);
            }

            List<Long> subcategoryIds = profiles.stream()
                    .map(CompanyProfile::getSubcategoryId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!subcategoryIds.isEmpty()) {
                List<SubCategory> subCategories = subCategoryRepository.findBySubCategoryIds(subcategoryIds);
                for (SubCategory subCat : subCategories) {
                    subcategoryMap.put(subCat.getSubCategoryId(), subCat);
                }
            }
        }

        List<CompanyOptionsResponse> results = companies.getContent().stream()
                .map(company -> toOptionsResponse(company, ratingMap.get(company.getCompanyId()), reviewCountMap.get(company.getCompanyId()), profileMap, subcategoryMap))
                .collect(Collectors.toList());

        long cappedTotalElements = Math.min(companies.getTotalElements(), MAX_TOTAL_ELEMENTS);
        return PageResponse.<CompanyOptionsResponse>builder()
                .result(results)
                .meta(PageResponse.Meta.builder()
                        .page(page).size(limit)
                        .totalElements(cappedTotalElements)
                        .totalPages(calculateTotalPages(cappedTotalElements, limit))
                        .hasNext(hasNextPage(page, limit, cappedTotalElements))
                        .hasPrevious(page > 0)
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

        // Create notification (the payload)
        Notification notification = Notification.builder()
                .type("COMPANY_REQUEST")
                .action("SUBMITTED")
                .referenceId(savedRequest.getCompanyRequestId())
                .actorId(userId)
                .createdAt(OffsetDateTime.now())
                .build();
        Notification savedNotification = notificationRepository.save(notification);

        // Create user_notification (recipient + read status)
        UserNotification userNotification = UserNotification.builder()
                .notification(savedNotification)
                .userId(userId)
                .isRead(false)
                .createdAt(OffsetDateTime.now())
                .build();
        userNotificationRepository.save(userNotification);

        auditService.record("COMPANY_REQUEST", savedRequest.getCompanyRequestId(), "SUBMITTED", userId);

        return toRequestResponse(savedRequest);
    }

    @Override
    public PageResponse<CompanyRequestResponse> getCompanyRequests(CompanyRequestStatus status, int page, int limit) {
        final int MAX_TOTAL_ELEMENTS = 1000;
        int requestedOffset = page * limit;
        if (requestedOffset >= MAX_TOTAL_ELEMENTS) {
            throw new BadRequestExceptions("limit exceeded");
        }
        Pageable pageable = PageRequest.of(page, limit);
        Page<CompanyRequestResponse> pageResult = (status == null
                ? companyRequestRepository.findAll(pageable)
                : companyRequestRepository.findByStatus(status, pageable))
                .map(this::toRequestResponse);
        long cappedTotalElements = Math.min(pageResult.getTotalElements(), MAX_TOTAL_ELEMENTS);
        return PageResponse.<CompanyRequestResponse>builder()
                .result(pageResult.getContent())
                .meta(PageResponse.Meta.builder()
                        .page(page).size(limit)
                        .totalElements(cappedTotalElements)
                        .totalPages(calculateTotalPages(cappedTotalElements, limit))
                        .hasNext(hasNextPage(page, limit, cappedTotalElements))
                        .hasPrevious(page > 0)
                        .build())
                .build();
    }

    @Override
    public CompanyRequestResponse reviewCompanyRequest(Long requestId, ReviewCompanyRequestRequest request) {
        Long reviewerId = securityUtils.getCurrentUserId();

        CompanyRequest companyRequest = companyRequestRepository.findById(requestId)
                .orElseThrow(() -> new BadRequestExceptions("Company request not found"));

        // Locking: prevent re-review if already finalized
        if (companyRequest.getStatus() == CompanyRequestStatus.APPROVED
                || companyRequest.getStatus() == CompanyRequestStatus.REJECTED) {
            throw new BadRequestExceptions("Company request has already been "
                    + companyRequest.getStatus().name().toLowerCase() + " and cannot be changed");
        }

        if (request.getStatus() == CompanyRequestStatus.APPROVED) {
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

        companyRequest.setStatus(request.getStatus());
        companyRequest.setReviewNote(request.getReviewNote());
        companyRequest.setReviewedAt(OffsetDateTime.now());
        companyRequest.setReviewedBy(reviewerId);
        companyRequest.setUpdatedAt(OffsetDateTime.now());
        companyRequest.setUpdatedBy(reviewerId);
        CompanyRequest savedRequest = companyRequestRepository.save(companyRequest);

        String actionLabel = request.getStatus() == CompanyRequestStatus.APPROVED ? "APPROVED" : "REJECTED";

        // Update the existing notification for the review action
        List<Notification> existingNotifications = notificationRepository.findByTypeAndReferenceId("COMPANY_REQUEST", requestId);
        if (existingNotifications.isEmpty()) {
            throw new BadRequestExceptions("Notification not found for company request");
        }
        Notification existingNotification = existingNotifications.get(0);
        existingNotification.setAction(actionLabel);
        existingNotification.setActorId(reviewerId);
        existingNotification.setCreatedAt(OffsetDateTime.now());
        Notification savedReviewNotif = notificationRepository.save(existingNotification);

        // Check if the user already has a UserNotification for this notification
        boolean userNotifExists = userNotificationRepository.existsByUserIdAndNotification_NotificationId(companyRequest.getCreatedBy(), savedReviewNotif.getNotificationId());
        if (!userNotifExists) {
            // Notify the original requester if not already notified
            UserNotification reviewUserNotif = UserNotification.builder()
                    .notification(savedReviewNotif)
                    .userId(companyRequest.getCreatedBy())
                    .isRead(false)
                    .createdAt(OffsetDateTime.now())
                    .build();
            userNotificationRepository.save(reviewUserNotif);
        } else {
            // Update the existing UserNotification's createdAt to reflect the latest action
            UserNotification existingUserNotif = userNotificationRepository.findByUserIdAndNotification_NotificationId(companyRequest.getCreatedBy(), savedReviewNotif.getNotificationId());
            if (existingUserNotif != null) {
                existingUserNotif.setCreatedAt(OffsetDateTime.now());
                userNotificationRepository.save(existingUserNotif);
            }
        }

        auditService.record("COMPANY_REQUEST", requestId, actionLabel, reviewerId, request.getReviewNote());

        return toRequestResponse(savedRequest);
    }

    @Override
    public CompanyRequestDetailResponse getCompanyRequestDetail(Long requestId) {
        Long currentUserId = securityUtils.getCurrentUserId();

        CompanyRequest companyRequest = companyRequestRepository.findById(requestId)
                .orElseThrow(() -> new BadRequestExceptions("Company request not found"));

        // Only the owner of the request may view its detail
        if (!companyRequest.getCreatedBy().equals(currentUserId)) {
            throw new CustomAccesDeniedExceptions("Access denied: you can only view your own company requests");
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

        final java.util.Map<Long, Double> ratingMap = new java.util.HashMap<>();
        final java.util.Map<Long, Long> reviewCountMap = new java.util.HashMap<>();
        final java.util.Map<Long, CompanyProfile> profileMap = new java.util.HashMap<>();
        final java.util.Map<Long, SubCategory> subcategoryMap = new java.util.HashMap<>();

        List<Object[]> ratings = internshipDetailRepository.findAverageRatingsByCompanyIds(companyIds);
        for (Object[] row : ratings) {
            Long companyId = ((Number) row[0]).longValue();
            Double avgRating = row[1] != null ? ((Number) row[1]).doubleValue() : null;
            ratingMap.put(companyId, avgRating);
        }

        List<Object[]> reviewCountData = internshipDetailRepository.findReviewCountsByCompanyIds(companyIds);
        for (Object[] row : reviewCountData) {
            Long companyId = ((Number) row[0]).longValue();
            Long reviewCount = ((Number) row[1]).longValue();
            reviewCountMap.put(companyId, reviewCount);
        }

        List<CompanyProfile> profiles = companyProfileRepository.findByCompanyIds(companyIds);
        for (CompanyProfile profile : profiles) {
            profileMap.put(profile.getCompanyId(), profile);
        }

        List<Long> subcategoryIds = profiles.stream()
                .map(CompanyProfile::getSubcategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!subcategoryIds.isEmpty()) {
            List<SubCategory> subCategories = subCategoryRepository.findBySubCategoryIds(subcategoryIds);
            for (SubCategory subCat : subCategories) {
                subcategoryMap.put(subCat.getSubCategoryId(), subCat);
            }
        }

        List<Company> companies = companyRepository.findAllById(companyIds);
        java.util.Map<Long, Company> companyMap = new java.util.HashMap<>();
        for (Company company : companies) {
            companyMap.put(company.getCompanyId(), company);
        }

        return searchResults.stream()
                .map(result -> {
                    Company company = companyMap.get(result.getCompanyId());
                    if (company == null) {
                        return result;
                    }
                    return toOptionsResponse(company, ratingMap.get(company.getCompanyId()), reviewCountMap.get(company.getCompanyId()), profileMap, subcategoryMap);
                })
                .collect(Collectors.toList());
    }

    public List<CompanyOptionsResponse> getTopCompaniesAvgRating() {
        List<Object[]> topCompanies = internshipDetailRepository.findTop10CompaniesByAverageRating();

        if (topCompanies.isEmpty()) {
            return List.of();
        }

        List<Long> companyIds = topCompanies.stream()
                .map(row -> ((Number) row[0]).longValue())
                .collect(Collectors.toList());

        final java.util.Map<Long, Double> ratingMap = new java.util.HashMap<>();
        final java.util.Map<Long, Long> reviewCountMap = new java.util.HashMap<>();
        final java.util.Map<Long, CompanyProfile> profileMap = new java.util.HashMap<>();
        final java.util.Map<Long, SubCategory> subcategoryMap = new java.util.HashMap<>();

        for (Object[] row : topCompanies) {
            Long companyId = ((Number) row[0]).longValue();
            Double avgRating = row[1] != null ? ((Number) row[1]).doubleValue() : null;
            ratingMap.put(companyId, avgRating);
        }

        List<Object[]> reviewCountData = internshipDetailRepository.findReviewCountsByCompanyIds(companyIds);
        for (Object[] row : reviewCountData) {
            Long companyId = ((Number) row[0]).longValue();
            Long reviewCount = ((Number) row[1]).longValue();
            reviewCountMap.put(companyId, reviewCount);
        }

        List<CompanyProfile> profiles = companyProfileRepository.findByCompanyIds(companyIds);
        for (CompanyProfile profile : profiles) {
            profileMap.put(profile.getCompanyId(), profile);
        }

        List<Long> subcategoryIds = profiles.stream()
                .map(CompanyProfile::getSubcategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!subcategoryIds.isEmpty()) {
            List<SubCategory> subCategories = subCategoryRepository.findBySubCategoryIds(subcategoryIds);
            for (SubCategory subCat : subCategories) {
                subcategoryMap.put(subCat.getSubCategoryId(), subCat);
            }
        }

        List<Company> companies = companyRepository.findAllById(companyIds);
        java.util.Map<Long, Company> companyMap = companies.stream()
                .collect(Collectors.toMap(Company::getCompanyId, c -> c));

        return topCompanies.stream()
                .map(row -> {
                    Long companyId = ((Number) row[0]).longValue();
                    Company company = companyMap.get(companyId);
                    if (company == null) return null;
                    return toOptionsResponse(company, ratingMap.get(companyId), reviewCountMap.get(companyId), profileMap, subcategoryMap);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private int calculateTotalPages(long totalElements, int limit) {
        return (int) Math.ceil((double) totalElements / limit);
    }

    private boolean hasNextPage(int page, int limit, long totalElements) {
        return (long) (page + 1) * limit < totalElements;
    }

    private CompanyOptionsResponse toOptionsResponse(Company company, Double rating, Long totalReviews, java.util.Map<Long, CompanyProfile> profileMap, java.util.Map<Long, SubCategory> subcategoryMap) {
        CompanyProfile profile = profileMap.get(company.getCompanyId());
        String subcategoryName = null;

        if (profile != null && profile.getSubcategoryId() != null) {
            SubCategory subCategory = subcategoryMap.get(profile.getSubcategoryId());
            subcategoryName = subCategory != null ? subCategory.getSubCategoryName() : null;
        }

        return CompanyOptionsResponse.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .companyAbbreviation(company.getCompanyAbbreviation())
                .website(profile != null ? profile.getWebsite() : null)
                .isPartner(profile != null ? profile.getIsPartner() : null)
                .subcategoryName(subcategoryName)
                .companySlug(company.getCompanySlug())
                .rating(rating)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
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

        List<CompanyRequestDetailResponse.DocumentResponse> documents = notificationRepository
                .findByTypeAndReferenceId("COMPANY_REQUEST", companyRequest.getCompanyRequestId())
                .stream()
                .flatMap(notif -> documentRepository.findByNotification_NotificationId(notif.getNotificationId()).stream())
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
        if (userId == null) return null;
        return userRepository.findByUserId(userId).map(User::getFirstName).orElse(null);
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

        String subcategoryName = null;
        if (profile.getSubcategoryId() != null) {
            SubCategory subCategory = subCategoryRepository.findById(profile.getSubcategoryId()).orElse(null);
            subcategoryName = subCategory != null ? subCategory.getSubCategoryName() : null;
        }

        Double rating = null;
        Long totalReviews = 0L;

        List<Object[]> ratingData = internshipDetailRepository.findAverageRatingsByCompanyIds(
                java.util.Collections.singletonList(company.getCompanyId())
        );
        if (!ratingData.isEmpty()) {
            rating = ((Number) ratingData.get(0)[1]).doubleValue();
        }

        List<Object[]> reviewCountData = internshipDetailRepository.findReviewCountsByCompanyIds(
                java.util.Collections.singletonList(company.getCompanyId())
        );
        if (!reviewCountData.isEmpty()) {
            Object[] row = reviewCountData.get(0);
            totalReviews = ((Number) row[1]).longValue();
        }

        return CompanyOptionsResponse.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .companyAbbreviation(company.getCompanyAbbreviation())
                .website(profile.getWebsite())
                .isPartner(profile.getIsPartner())
                .subcategoryName(subcategoryName)
                .companySlug(company.getCompanySlug())
                .rating(rating)
                .totalReviews(totalReviews)
                .build();
    }

    private String generateAbbreviation(String companyName) {
        // Simple abbreviation: take uppercase initials
        String[] words = companyName.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        return sb.toString();
    }
}
