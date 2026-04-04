package com.example.skripsi.services;

import com.example.skripsi.configs.*;
import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.user.*;
import com.example.skripsi.models.constant.*;
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
    private final UserCertificatesRepository userCertificatesRepository;
    private final MinioConfig minioConfig;

    public UserService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       SecurityUtils securityUtils,
                       NotificationRepository notificationRepository,
                       UserNotificationRepository userNotificationRepository,
                       UserCertificateRequestRepository userCertificateRequestRepository,
                       @Lazy AuditService auditService,
                       UserCertificatesRepository userCertificatesRepository, MinioConfig minioConfig){
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
        this.notificationRepository = notificationRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.userCertificateRequestRepository = userCertificateRequestRepository;
        this.auditService = auditService;
        this.userCertificatesRepository = userCertificatesRepository;
        this.minioConfig = minioConfig;
    }

    @Override
    public List<UserResponse> getAllUserByUserPrivilege() {
        Long userId = securityUtils.getCurrentUserId();
        log.info("[getAllUserByUserPrivilege] userId={}", userId);

        var privilegeLevel = userRepository.getUserPrivilege(userId)
                .orElseThrow(() -> {
                    log.warn("[getAllUserByUserPrivilege] no privilege found userId={}", userId);
                    return new CustomAccessDeniedException("Insufficient privileges");
                })
                .toLowerCase();

        log.info("[getAllUserByUserPrivilege] userId={} privilegeLevel={}", userId, privilegeLevel);
        var users = userRepository.getUserByUserPrivilege(privilegeLevel, userId);

        if (users.isEmpty()) {
            return List.of();
        }

        var userIds = users.stream().map(User::getUserId).toList();
        var userProfiles = userProfileRepository.findAllByUser_UserIdIn(userIds);
        var profileMap = userProfiles.stream()
                .collect(Collectors.toMap(
                        p -> p.getUser().getUserId(), // key
                        p -> p, // values
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

        return toCertificateResponse(request.getIssuer(), request.getCertificateUrl());
    }

    public CertificateResponse reviewCertificateRequest(Long requestId, ReviewCertificateRequest request) {
        Long reviewerId = securityUtils.getCurrentUserId();
        log.info("[reviewCertificateRequest] requestId={} status={} reviewerId={}", requestId, request.getStatus(), reviewerId);

        UserCertificateRequest userCertificateRequest = userCertificateRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("[reviewCertificateRequest] request not found requestId={}", requestId);
                    return new InvalidCredentialsException(MessageConstants.NotFound.REQUEST_DOCUMENT_NOT_FOUND);
                });

        if (!DocumentTypeConstants.CERTIFICATE.equals(userCertificateRequest.getDocumentType())) {
            throw new InvalidCredentialsException(MessageConstants.Validation.INVALID_DOCUMENT_TYPE);
        }

        if (ActionConstants.APPROVED.equals(userCertificateRequest.getStatus()) || ActionConstants.REJECTED.equals(userCertificateRequest.getStatus())) {
            log.warn("[reviewCertificateRequest] already finalized requestId={} currentStatus={}", requestId, userCertificateRequest.getStatus());
            throw new InvalidCredentialsException(MessageConstants.Certificate.CERTIFICATE_REQUEST_ALREADY_FINALIZED + userCertificateRequest.getStatus().toLowerCase() + MessageConstants.Certificate.CANNOT_BE_CHANGED);
        }

        if (ActionConstants.APPROVED.equals(request.getStatus())) {
            UserProfile userProfile = userProfileRepository.findById(userCertificateRequest.getNotification().getReferenceId())
                    .orElseThrow(() -> new InvalidCredentialsException(MessageConstants.NotFound.USER_PROFILE_NOT_FOUND));

            UserCertificates userCertificate = UserCertificates.builder()
                    .userProfile(userProfile)
                    .issuer(userCertificateRequest.getDocumentName()) // assuming issuer is stored in documentName or need to adjust
                    .certificatesUrl(userCertificateRequest.getDocumentUrl())
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

        return toCertificateResponse(userCertificateRequest.getDocumentName(), userCertificateRequest.getDocumentUrl());
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

    private UserResponse toResponse(User user, UserProfile profile) {
        var roleName = user.getRoles().stream()
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
        var roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getRoleName)
                .orElse("USER")
                .toLowerCase();

        var major = userProfile.getMajor();
        var regionName = major != null && major.getRegion() != null ? major.getRegion().getRegionName() : null;
        var deptName = major != null && major.getDepartment() != null ? major.getDepartment().getDeptName() : null;

        var certificateEntities = userCertificatesRepository.findByUserProfile_UserProfileId(userProfile.getUserProfileId());
        var certificate = certificateEntities.stream()
                .map(cert -> CertificateUrlResponse.builder()
                        .url(buildMinioProxyUrl(cert.getCertificatesUrl()))
                        .build())
                .toList();

        return UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(userProfile.getPhoneNumber())
                .role(roleName)
                .regionName(regionName)
                .deptName(deptName)
                .majorName(major != null ? major.getMajorName() : null)
                .certificate(certificate)
                .build();
    }

    private String buildMinioProxyUrl(String certificatesUrl) {
        return minioConfig.getProxyEndpoint() + "/" + certificatesUrl;
    }

    private CertificateResponse toCertificateResponse(String issuer, String url) {
        return CertificateResponse.builder()
                .userCertificateId(null)
                .issuer(issuer)
                .certificatesUrl(url)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    private CertificateRequestDetailResponse toCertificateRequestDetailResponse(
            UserCertificateRequest userCertificateRequest, Notification notification) {
        return CertificateRequestDetailResponse.builder()
                .requestDetails(CertificateRequestDetailResponse.RequestDetails.builder()
                        .requestId(userCertificateRequest.getDocumentId())
                        .certificateName(userCertificateRequest.getDocumentName())
                        .certificatesUrl(userCertificateRequest.getDocumentUrl())
                        .fileSize(userCertificateRequest.getFileSize())
                        .submittedAt(userCertificateRequest.getCreatedAt())
                        .submittedBy(resolveUserName(userCertificateRequest.getCreatedBy()))
                        .build())
                .reviewInformation(CertificateRequestDetailResponse.ReviewInformation.builder()
                        .status(userCertificateRequest.getStatus())
                        .reviewedAt(userCertificateRequest.getUploadedAt())
                        .reviewNote(null)
                        .reviewedBy(userCertificateRequest.getUploadedAt() != null ? resolveUserName(notification.getActorId()) : null)
                        .build())
                .build();
    }
}
