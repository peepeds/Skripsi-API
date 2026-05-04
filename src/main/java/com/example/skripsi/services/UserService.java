package com.example.skripsi.services;

import com.example.skripsi.configs.*;
import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.CursorPageResponse;
import com.example.skripsi.models.user.*;
import com.example.skripsi.models.constant.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.example.skripsi.repositories.*;
import com.example.skripsi.securities.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final SecurityUtils securityUtils;
    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final UserCertificateRequestRepository userCertificateRequestRepository;
    private final AuditService auditService;
    private final AuditLogRepository auditLogRepository;
    private final UserCertificatesRepository userCertificatesRepository;
    private final MinioConfig minioConfig;
    private final IMinioService minioService;
    private final CompanyRepository companyRepository;

    public UserService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       SecurityUtils securityUtils,
                       NotificationRepository notificationRepository,
                       UserNotificationRepository userNotificationRepository,
                       UserCertificateRequestRepository userCertificateRequestRepository,
                       @Lazy AuditService auditService,
                       AuditLogRepository auditLogRepository,
                       UserCertificatesRepository userCertificatesRepository,
                       MinioConfig minioConfig,
                       IMinioService minioService,
                       CompanyRepository companyRepository){
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
        this.notificationRepository = notificationRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.userCertificateRequestRepository = userCertificateRequestRepository;
        this.auditService = auditService;
        this.auditLogRepository = auditLogRepository;
        this.userCertificatesRepository = userCertificatesRepository;
        this.minioConfig = minioConfig;
        this.minioService = minioService;
        this.companyRepository = companyRepository;
    }

    @Override
    public List<UserResponse> getAllUserByUserPrivilege() {
        Long userId = securityUtils.getCurrentUserId();
        log.info("[getAllUserByUserPrivilege] userId={}", userId);

        var privilegeLevel = userRepository.getUserPrivilege(userId)
                                .map(String::toLowerCase)
                                .orElse("admin");

        log.info("[getAllUserByUserPrivilege] userId={} privilegeLevel={}", userId, privilegeLevel);
        var users = "admin".equalsIgnoreCase(privilegeLevel)
                ? userRepository.findAll()
                : userRepository.getUserByUserPrivilege(privilegeLevel, userId);

        if (users.isEmpty()) {
            return List.of();
        }

        var userIds = users.stream().map(User::getUserId).toList();
        var userProfiles = userProfileRepository.findAllByUser_UserIdIn(userIds);
        var profileMap = userProfiles.stream()
                .collect(Collectors.toMap(
                        p -> p.getUser().getUserId(),
                        p -> p,
                        (existing, replacement) -> existing
                ));

        return users.stream()
                .map(user -> toResponse(user, profileMap.get(user.getUserId())))
                .toList();
    }

    public Boolean emailExists(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public UserResponse getUserProfile() {
        Long userId = securityUtils.getCurrentUserId();
        log.info("[getUserProfile] userId={}", userId);
        UserProfile userProfile = userProfileRepository.findByUserUserId(userId)
                .orElseThrow(() -> {
                    log.warn("[getUserProfile] profile not found userId={}", userId);
                    return new InvalidCredentialsException("User profile not found");
                });

        return toUserResponse(userProfile);
    }

    public CertificateResponse submitCertificateRequest(CreateCertificateRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("[submitCertificateRequest] userId={} certificateName={}", userId, request.getCertificateName());
        UserProfile userProfile = userProfileRepository.findByUserUserId(userId)
                .orElseThrow(() -> {
                    log.warn("[submitCertificateRequest] profile not found userId={}", userId);
                    return new InvalidCredentialsException(MessageConstants.NotFound.USER_PROFILE_NOT_FOUND);
                });

        // Create notification
        Notification notification = Notification.builder()
                .type(EntityTypeConstants.UPLOAD_CERTIFICATES)
                .action(ActionConstants.SUBMITTED)
                .referenceId(userProfile.getUserProfileId())
                .actorId(userId)
                .createdAt(OffsetDateTime.now())
                .build();
        Notification savedNotification = notificationRepository.save(notification);

        // Create user_notification
        UserNotification userNotification = UserNotification.builder()
                .notification(savedNotification)
                .userId(userId)
                .isRead(false)
                .createdAt(OffsetDateTime.now())
                .build();
        userNotificationRepository.save(userNotification);

        // Create user_certificate_request
        UserCertificateRequest userCertificateRequest = UserCertificateRequest.builder()
                .notification(savedNotification)
                .documentName(request.getCertificateName())
                .documentUrl(request.getCertificateUrl())
                .documentType(DocumentTypeConstants.CERTIFICATE)
                .fileSize(request.getFileSize())
                .issuer(request.getIssuer())
                .status(StatusConstants.PENDING)
                .createdAt(OffsetDateTime.now())
                .createdBy(userId)
                .build();
        userCertificateRequestRepository.save(userCertificateRequest);

        // Update notification referenceId to documentId
        savedNotification.setReferenceId(userCertificateRequest.getDocumentId());
        notificationRepository.save(savedNotification);

        auditService.record(EntityTypeConstants.UPLOAD_CERTIFICATES, userCertificateRequest.getDocumentId(), ActionConstants.SUBMITTED, userId);
        log.info("[submitCertificateRequest] created documentId={} userId={}", userCertificateRequest.getDocumentId(), userId);

        return toCertificateResponse(request.getIssuer(), request.getCertificateUrl(), request.getCertificateName());
    }

    public CertificateResponse reviewCertificateRequest(Long requestId, ReviewCertificateRequest request) {
        Long reviewerId = securityUtils.getCurrentUserId();
        log.info("[reviewCertificateRequest] requestId={} status={} reviewerId={}", requestId, request.getStatus(), reviewerId);

        UserCertificateRequest userCertificateRequest = userCertificateRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("[reviewCertificateRequest] request not found requestId={}", requestId);
                    return new ResourceNotFoundException(MessageConstants.NotFound.REQUEST_DOCUMENT_NOT_FOUND);
                });

        if (!DocumentTypeConstants.CERTIFICATE.equals(userCertificateRequest.getDocumentType())) {
            throw new BadRequestExceptions(MessageConstants.Validation.INVALID_DOCUMENT_TYPE);
        }

        if (ActionConstants.APPROVED.equals(userCertificateRequest.getStatus()) || ActionConstants.REJECTED.equals(userCertificateRequest.getStatus())) {
            log.warn("[reviewCertificateRequest] already finalized requestId={} currentStatus={}", requestId, userCertificateRequest.getStatus());
            throw new BadRequestExceptions(MessageConstants.Certificate.CERTIFICATE_REQUEST_ALREADY_FINALIZED + userCertificateRequest.getStatus().toLowerCase() + MessageConstants.Certificate.CANNOT_BE_CHANGED);
        }

        if (ActionConstants.APPROVED.equals(request.getStatus())) {
            UserProfile userProfile = userProfileRepository.findByUserUserId(userCertificateRequest.getCreatedBy())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.NotFound.USER_PROFILE_NOT_FOUND));

            UserCertificates userCertificate = UserCertificates.builder()
                    .userProfile(userProfile)
                    .issuer(userCertificateRequest.getIssuer())
                    .certificatesUrl(userCertificateRequest.getDocumentUrl())
                    .certificateName(userCertificateRequest.getDocumentName())
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();
            userCertificatesRepository.save(userCertificate);
        }

        userCertificateRequest.setStatus(request.getStatus());
        userCertificateRequest.setUploadedAt(OffsetDateTime.now());
        userCertificateRequestRepository.save(userCertificateRequest);

        String actionLabel = ActionConstants.APPROVED.equals(request.getStatus()) ? ActionConstants.APPROVED : ActionConstants.REJECTED;

        // Update the existing notification for the review action
        Notification notification = userCertificateRequest.getNotification();
        notification.setAction(actionLabel);
        notification.setActorId(reviewerId);
        notification.setCreatedAt(OffsetDateTime.now());
        Notification savedReviewNotif = notificationRepository.save(notification);

        // Check if the user already has a UserNotification for this notification
        boolean userNotifExists = userNotificationRepository.existsByUserIdAndNotification_NotificationId(userCertificateRequest.getCreatedBy(), savedReviewNotif.getNotificationId());

        if (!userNotifExists) {
            // Notify the original requester if not already notified
            UserNotification reviewUserNotif = UserNotification.builder()
                    .notification(savedReviewNotif)
                    .userId(userCertificateRequest.getCreatedBy())
                    .isRead(false)
                    .createdAt(OffsetDateTime.now())
                    .build();
            userNotificationRepository.save(reviewUserNotif);
        } else {
            // Update the existing UserNotification's createdAt to reflect the latest action
            UserNotification existingUserNotif = userNotificationRepository.findByUserIdAndNotification_NotificationId(userCertificateRequest.getCreatedBy(), savedReviewNotif.getNotificationId());

            if (existingUserNotif != null) {
                existingUserNotif.setCreatedAt(OffsetDateTime.now());
                userNotificationRepository.save(existingUserNotif);
            }
        }

        auditService.record(EntityTypeConstants.UPLOAD_CERTIFICATES, requestId, actionLabel, reviewerId, request.getReviewNote());
        log.info("[reviewCertificateRequest] done requestId={} action={} reviewerId={}", requestId, actionLabel, reviewerId);

        return toCertificateResponse(userCertificateRequest.getIssuer(), userCertificateRequest.getDocumentUrl(), userCertificateRequest.getDocumentName());
    }

    public CertificateRequestDetailResponse getCertificateRequestDetail(Long requestId) {
        Long currentUserId = securityUtils.getCurrentUserId();

        UserCertificateRequest userCertificateRequest = userCertificateRequestRepository.findById(requestId)
                .orElseThrow(() -> new InvalidCredentialsException("Request document not found"));

        Notification notification = userCertificateRequest.getNotification();

        if (!EntityTypeConstants.UPLOAD_CERTIFICATES.equals(notification.getType())) {
            throw new InvalidCredentialsException("Invalid notification type");
        }

        // Only the owner of the request may view its detail

        if (!userCertificateRequest.getCreatedBy().equals(currentUserId)) {
            throw new CustomAccessDeniedException("Access denied: you can only view your own certificate requests");
        }

        return toCertificateRequestDetailResponse(userCertificateRequest, notification);
    }

    @Override
    public String resolveUserName(Long userId) {
        if (userId == null) return null;
        return userRepository.findByUserId(userId).map(User::getFirstName).orElse(null);
    }

    @Override
    public Map<Long, String> getUserNameMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        List<User> users = userRepository.findByUserIdMap(userIds);
        Map<Long, String> result = new java.util.HashMap<>();
        users.forEach(user -> result.put(user.getUserId(), user.getFirstName()));
        return result;
    }

    @Override
    public Boolean userExists(Long userId) {
        if (userId == null) return false;
        return userRepository.existsById(userId);
    }

    @Override
    public Optional<User> findUserById(Long userId) {
        if (userId == null) return Optional.empty();
        return userRepository.findById(userId);
    }

    @Override
    public List<Notification> findNotificationsByTypeAndReferenceId(String type, Long referenceId) {
        if (type == null || referenceId == null) return List.of();
        return notificationRepository.findByTypeAndReferenceId(type, referenceId);
    }

    @Override
    public List<UserCertificateRequest> findUserCertificateRequestsByNotificationId(Long notificationId) {
        if (notificationId == null) return List.of();
        return userCertificateRequestRepository.findByNotification_NotificationId(notificationId);
    }

    @Override
    public Boolean isCertificateRequestOwner(Long requestId, Long userId) {
        if (requestId == null || userId == null) return false;
        return userCertificateRequestRepository.findById(requestId)
                .map(req -> req.getCreatedBy().equals(userId))
                .orElse(false);
    }

    @Override
    public List<CertificateResponse> getMyCertificates() {
        Long userId = securityUtils.getCurrentUserId();
        log.info("[getMyCertificates] userId={}", userId);
        UserProfile userProfile = userProfileRepository.findByUserUserId(userId)
                .orElseThrow(() -> {
                    log.warn("[getMyCertificates] profile not found userId={}", userId);
                    return new InvalidCredentialsException(MessageConstants.NotFound.USER_PROFILE_NOT_FOUND);
                });

        return userCertificatesRepository.findByUserProfile_UserProfileId(userProfile.getUserProfileId())
                .stream()
                .map(cert -> {
                    String url;
                    try {
                        url = minioService.getPresignedViewUrl(cert.getCertificatesUrl());
                    } catch (Exception e) {
                        log.warn("[getMyCertificates] failed to generate presigned url for cert={}", cert.getUserCertificateId(), e);
                        url = buildMinioProxyUrl(cert.getCertificatesUrl());
                    }
                    String issuerName = companyRepository.findById(cert.getIssuer())
                            .map(Company::getCompanyName)
                            .orElse(null);
                    return CertificateResponse.builder()
                            .userCertificateId(cert.getUserCertificateId())
                            .issuer(cert.getIssuer())
                            .issuerName(issuerName)
                            .certificatesUrl(url)
                            .certificateName(cert.getCertificateName())
                            .createdAt(cert.getCreatedAt())
                            .updatedAt(cert.getUpdatedAt())
                            .build();
                })
                .toList();
    }

    @Override
    public CursorPageResponse<CertificateRequestListResponse> getCertificateRequests(String status, Long cursor, int limit) {
        log.info("[getCertificateRequests] status={} cursor={} limit={}", status, cursor, limit);
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<UserCertificateRequest> requests = status == null
                ? userCertificateRequestRepository.findPageFromCursor(cursor, pageable)
                : userCertificateRequestRepository.findPageByStatusFromCursor(status, cursor, pageable);

        boolean hasMore = requests.size() > limit;
        List<UserCertificateRequest> pageRequests = hasMore ? requests.subList(0, limit) : requests;

        List<CertificateRequestListResponse> items = pageRequests.stream()
                .map(this::toCertificateRequestListResponse)
                .collect(Collectors.toList());

        Long nextCursor = hasMore && !items.isEmpty()
                ? pageRequests.get(pageRequests.size() - 1).getDocumentId()
                : null;

        return CursorPageResponse.<CertificateRequestListResponse>builder()
                .result(items)
                .meta(CursorPageResponse.Meta.builder()
                        .nextCursor(nextCursor)
                        .previousCursor(cursor)
                        .size(items.size())
                        .hasMore(hasMore)
                        .build())
                .build();
    }

    private CertificateRequestListResponse toCertificateRequestListResponse(UserCertificateRequest req) {
        boolean isReviewed = req.getUploadedAt() != null;
        Notification notification = req.getNotification();

        AuditLog reviewLog = auditLogRepository
                .findTopByEntityTypeAndEntityIdAndActionInOrderByTimestampDesc(
                        EntityTypeConstants.UPLOAD_CERTIFICATES,
                        req.getDocumentId(),
                        List.of(ActionConstants.APPROVED, ActionConstants.REJECTED))
                .orElse(null);

        return CertificateRequestListResponse.builder()
                .requestId(req.getDocumentId())
                .certificateName(req.getDocumentName())
                .status(req.getStatus())
                .createdAt(req.getCreatedAt())
                .submittedBy(resolveUserName(req.getCreatedBy()))
                .reviewedAt(isReviewed ? req.getUploadedAt() : null)
                .reviewedBy(isReviewed && notification != null ? resolveUserName(notification.getActorId()) : null)
                .reviewNote(reviewLog != null ? reviewLog.getNotes() : null)
                .build();
    }

    private UserResponse toResponse(User user, UserProfile profile) {
        var roleName = Optional.ofNullable(user.getRoles())
                .orElse(java.util.Collections.emptySet())
                .stream()
                .findFirst()
                .map(Role::getRoleName)
                .orElse("USER")
                .toLowerCase();

        var phoneNumber = Optional.ofNullable(profile)
                .map(UserProfile::getPhoneNumber)
                .orElse(null);

        var major = Optional.ofNullable(profile)
                .map(UserProfile::getMajor)
                .orElse(null);

        var regionName = Optional.ofNullable(major)
                .map(Major::getRegion)
                .map(Region::getRegionName)
                .orElse(null);

        var deptName = Optional.ofNullable(major)
                .map(Major::getDepartment)
                .map(Department::getDeptName)
                .orElse(null);

        var majorName = Optional.ofNullable(major)
                .map(Major::getMajorName)
                .orElse(null);

        return UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName("%s %s".formatted(
                        user.getFirstName(),
                        Optional.ofNullable(user.getLastName()).orElse("").trim()
                ))
                .email(user.getEmail())
                .phoneNumber(phoneNumber)
                .regionName(regionName)
                .deptName(deptName)
                .majorName(majorName)
                .role(roleName)
                .build();
    }

    private UserResponse toUserResponse(UserProfile userProfile) {
        User user = userProfile.getUser();
        var roleName = Optional.ofNullable(user.getRoles())
                .orElse(java.util.Collections.emptySet())
                .stream()
                .findFirst()
                .map(Role::getRoleName)
                .orElse("USER")
                .toLowerCase();

        var major = userProfile.getMajor();
        var regionName = major != null && major.getRegion() != null ? major.getRegion().getRegionName() : null;
        var deptName = major != null && major.getDepartment() != null ? major.getDepartment().getDeptName() : null;

        return UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName("%s %s".formatted(
                        user.getFirstName(),
                        Optional.ofNullable(user.getLastName()).orElse("").trim()
                ))
                .email(user.getEmail())
                .phoneNumber(userProfile.getPhoneNumber())
                .role(roleName)
                .regionName(regionName)
                .deptName(deptName)
                .majorName(major != null ? major.getMajorName() : null)
                .build();
    }

    private String buildMinioProxyUrl(String certificatesUrl) {
        return minioConfig.getProxyEndpoint() + "/" + certificatesUrl;
    }

    private CertificateResponse toCertificateResponse(Long issuer, String url, String certificateName) {
        return CertificateResponse.builder()
                .userCertificateId(null)
                .issuer(issuer)
                .certificatesUrl(url)
                .certificateName(certificateName)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    private CertificateRequestDetailResponse toCertificateRequestDetailResponse(
            UserCertificateRequest userCertificateRequest, Notification notification) {
        String documentUrl;
        try {
            documentUrl = minioService.getPresignedViewUrl(userCertificateRequest.getDocumentUrl());
        } catch (Exception e) {
            log.warn("[getCertificateRequestDetail] failed to generate presigned url for requestId={}", userCertificateRequest.getDocumentId(), e);
            documentUrl = buildMinioProxyUrl(userCertificateRequest.getDocumentUrl());
        }

        AuditLog reviewLog = auditLogRepository
                .findTopByEntityTypeAndEntityIdAndActionInOrderByTimestampDesc(
                        EntityTypeConstants.UPLOAD_CERTIFICATES,
                        userCertificateRequest.getDocumentId(),
                        List.of(ActionConstants.APPROVED, ActionConstants.REJECTED))
                .orElse(null);

        boolean isReviewed = userCertificateRequest.getUploadedAt() != null;

        return CertificateRequestDetailResponse.builder()
                .requestDetails(CertificateRequestDetailResponse.RequestDetails.builder()
                        .requestId(userCertificateRequest.getDocumentId())
                        .certificateName(userCertificateRequest.getDocumentName())
                        .certificatesUrl(documentUrl)
                        .fileSize(userCertificateRequest.getFileSize())
                        .submittedAt(userCertificateRequest.getCreatedAt())
                        .submittedBy(resolveUserName(userCertificateRequest.getCreatedBy()))
                        .build())
                .reviewInformation(CertificateRequestDetailResponse.ReviewInformation.builder()
                        .status(userCertificateRequest.getStatus())
                        .reviewedAt(isReviewed ? userCertificateRequest.getUploadedAt() : null)
                        .reviewedBy(isReviewed ? resolveUserName(notification.getActorId()) : null)
                        .reviewNote(reviewLog != null ? reviewLog.getNotes() : null)
                        .build())
                .build();
    }

}
