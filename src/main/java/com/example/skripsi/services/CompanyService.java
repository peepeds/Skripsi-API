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

    public CompanyService(CompanyRepository companyRepository,
                          CompanyRequestRepository companyRequestRepository,
                          NotificationRepository notificationRepository,
                          UserNotificationRepository userNotificationRepository,
                          UserCertificateRequestRepository documentRepository,
                          AuditService auditService,
                          SecurityUtils securityUtils,
                          UserRepository userRepository,
                          CompanyProfileRepository companyProfileRepository) {
        this.companyRepository = companyRepository;
        this.companyRequestRepository = companyRequestRepository;
        this.notificationRepository = notificationRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.documentRepository = documentRepository;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
        this.userRepository = userRepository;
        this.companyProfileRepository = companyProfileRepository;
    }

    @Override
    public PageResponse<CompanyOptionsResponse> getCompany(int page, int limit) {
        final int MAX_TOTAL_ELEMENTS = 1000;
        int requestedOffset = page * limit;
        if (requestedOffset >= MAX_TOTAL_ELEMENTS) {
            throw new BadRequestExceptions("limit exceeded");
        }
        Pageable pageable = PageRequest.of(page, limit);
        Page<CompanyOptionsResponse> pageResult = companyRepository.findAll(pageable).map(this::toOptionsResponse);
        long cappedTotalElements = Math.min(pageResult.getTotalElements(), MAX_TOTAL_ELEMENTS);
        return PageResponse.<CompanyOptionsResponse>builder()
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
        return companyRepository.searchCompanies(search);
    }

    private int calculateTotalPages(long totalElements, int limit) {
        return (int) Math.ceil((double) totalElements / limit);
    }

    private boolean hasNextPage(int page, int limit, long totalElements) {
        return (long) (page + 1) * limit < totalElements;
    }

    private CompanyOptionsResponse toOptionsResponse(Company company) {
        CompanyProfile profile = companyProfileRepository.findByCompanyId(company.getCompanyId());
        return CompanyOptionsResponse.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .companyAbbreviation(company.getCompanyAbbreviation())
                .website(profile != null ? profile.getWebsite() : null)
                .isPartner(profile != null ? profile.getIsPartner() : null)
                .companySlug(company.getCompanySlug())
                .build();
    }

    private CompanyRequestResponse toRequestResponse(CompanyRequest companyRequest) {
        return CompanyRequestResponse.builder()
                .companyRequestId(companyRequest.getCompanyRequestId())
                .companyName(companyRequest.getCompanyName())
                .companyAbbreviation(companyRequest.getCompanyAbbreviation())
                .website(companyRequest.getWebsite())
                .isPartner(companyRequest.getIsPartner())
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
    public CompanyProfileDetailResponse getCompanyBySlug(String slug) {
        Company company = companyRepository.findByCompanySlug(slug);
        if (company == null) {
            throw new BadRequestExceptions("Company not found");
        }
        CompanyProfile profile = companyProfileRepository.findByCompanyId(company.getCompanyId());
        if (profile == null) {
            throw new BadRequestExceptions("Company profile not found");
        }
        return CompanyProfileDetailResponse.builder()
                .companyName(company.getCompanyName())
                .companyAbbreviation(company.getCompanyAbbreviation())
                .bio(profile.getBio())
                .website(profile.getWebsite())
                .isPartner(profile.getIsPartner())
                .createdAt(profile.getCreatedAt())
                .createdBy(profile.getCreatedBy())
                .updatedAt(profile.getUpdatedAt())
                .updatedBy(profile.getUpdatedBy())
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
