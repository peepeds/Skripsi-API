package com.example.skripsi.services;

import com.example.skripsi.configs.MinioConfig;
import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.CustomAccesDeniedExceptions;
import com.example.skripsi.exceptions.InvalidCredentialsException;
import com.example.skripsi.interfaces.IUserService;
import com.example.skripsi.models.user.CreateCertificateRequest;
import com.example.skripsi.models.user.CertificateResponse;
import com.example.skripsi.models.user.ReviewCertificateRequest;
import com.example.skripsi.models.user.CertificateRequestDetailResponse;
import com.example.skripsi.models.user.UserResponse;
import com.example.skripsi.models.user.CertificateUrlResponse;
import com.example.skripsi.repositories.UserProfileRepository;
import com.example.skripsi.repositories.UserRepository;
import com.example.skripsi.repositories.NotificationRepository;
import com.example.skripsi.repositories.UserNotificationRepository;
import com.example.skripsi.repositories.RequestDocumentRepository;
import com.example.skripsi.repositories.UserCertificatesRepository;
import com.example.skripsi.securities.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Transactional
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final SecurityUtils securityUtils;
    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final RequestDocumentRepository requestDocumentRepository;
    private final AuditService auditService;
    private final UserCertificatesRepository userCertificatesRepository;
    private final MinioConfig minioConfig;

    public UserService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       SecurityUtils securityUtils,
                       NotificationRepository notificationRepository,
                       UserNotificationRepository userNotificationRepository,
                       RequestDocumentRepository requestDocumentRepository,
                       AuditService auditService,
                       UserCertificatesRepository userCertificatesRepository, MinioConfig minioConfig){
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
        this.notificationRepository = notificationRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.requestDocumentRepository = requestDocumentRepository;
        this.auditService = auditService;
        this.userCertificatesRepository = userCertificatesRepository;
        this.minioConfig = minioConfig;
    }

    @Override
    public List<UserResponse> getAllUserByUserPrivilege() {

        Long userId = securityUtils.getCurrentUserId();

        var privilegeLevel = userRepository.getUserPrivilege(userId)
                .orElseThrow(() -> new CustomAccesDeniedExceptions("Insufficient privileges"))
                .toLowerCase();

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
        UserProfile userProfile = userProfileRepository.findByUserUserId(userId)
                .orElseThrow(() ->
                        new InvalidCredentialsException("User profile not found")
                );

        return toUserResponse(userProfile);
    }

    private String buildMinioProxyUrl(String certificatesUrl) {
        // Assuming certificatesUrl is the object key, not a full URL
        return minioConfig.getProxyEndpoint() + "/" + certificatesUrl;
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

        // Fetch certificates for this user profile
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

    private List<CertificateResponse> toCertificateResponses(List<UserCertificates> userCertificates) {
        if (userCertificates == null) return null;
        return userCertificates.stream()
                .map(cert -> CertificateResponse.builder()
                        .userCertificateId(cert.getUserCertificateId())
                        .issuer(cert.getIssuer())
                        .certificatesUrl(cert.getCertificatesUrl())
                        .createdAt(cert.getCreatedAt())
                        .updatedAt(cert.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public CertificateResponse submitCertificateRequest(CreateCertificateRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        UserProfile userProfile = userProfileRepository.findByUserUserId(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User profile not found"));

        // Create notification
        Notification notification = Notification.builder()
                .type("UPLOAD_CERTIFICATES")
                .action("SUBMITTED")
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

        // Create request_document
        RequestDocument requestDocument = RequestDocument.builder()
                .notification(savedNotification)
                .documentName(request.getCertificateName())
                .documentUrl(request.getCertificateUrl())
                .documentType("CERTIFICATE")
                .fileSize(request.getFileSize())
                .status("PENDING")
                .createdAt(OffsetDateTime.now())
                .createdBy(userId)
                .build();
        requestDocumentRepository.save(requestDocument);

        // Update notification referenceId to documentId
        savedNotification.setReferenceId(requestDocument.getDocumentId());
        notificationRepository.save(savedNotification);

        auditService.record("UPLOAD_CERTIFICATES", requestDocument.getDocumentId(), "SUBMITTED", userId);

        return CertificateResponse.builder()
                .userCertificateId(null) // belum ada
                .issuer(request.getIssuer())
                .certificatesUrl(request.getCertificateUrl())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public CertificateResponse reviewCertificateRequest(Long requestId, ReviewCertificateRequest request) {
        Long reviewerId = securityUtils.getCurrentUserId();

        RequestDocument requestDocument = requestDocumentRepository.findById(requestId)
                .orElseThrow(() -> new InvalidCredentialsException("Request document not found"));

        if (!"CERTIFICATE".equals(requestDocument.getDocumentType())) {
            throw new InvalidCredentialsException("Invalid document type");
        }

        // Locking: prevent re-review if already finalized
        if ("APPROVED".equals(requestDocument.getStatus()) || "REJECTED".equals(requestDocument.getStatus())) {
            throw new InvalidCredentialsException("Certificate request has already been " + requestDocument.getStatus().toLowerCase() + " and cannot be changed");
        }

        if ("APPROVED".equals(request.getStatus())) {
            UserProfile userProfile = userProfileRepository.findById(requestDocument.getNotification().getReferenceId())
                    .orElseThrow(() -> new InvalidCredentialsException("User profile not found"));

            UserCertificates userCertificate = UserCertificates.builder()
                    .userProfile(userProfile)
                    .issuer(requestDocument.getDocumentName()) // assuming issuer is stored in documentName or need to adjust
                    .certificatesUrl(requestDocument.getDocumentUrl())
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();
            userCertificatesRepository.save(userCertificate);
        }

        requestDocument.setStatus(request.getStatus());
        requestDocument.setUploadedAt(OffsetDateTime.now());
        requestDocumentRepository.save(requestDocument);

        String actionLabel = "APPROVED".equals(request.getStatus()) ? "APPROVED" : "REJECTED";

        // Update the existing notification for the review action
        Notification notification = requestDocument.getNotification();
        notification.setAction(actionLabel);
        notification.setActorId(reviewerId);
        notification.setCreatedAt(OffsetDateTime.now());
        Notification savedReviewNotif = notificationRepository.save(notification);

        // Check if the user already has a UserNotification for this notification
        boolean userNotifExists = userNotificationRepository.existsByUserIdAndNotification_NotificationId(requestDocument.getCreatedBy(), savedReviewNotif.getNotificationId());
        if (!userNotifExists) {
            // Notify the original requester if not already notified
            UserNotification reviewUserNotif = UserNotification.builder()
                    .notification(savedReviewNotif)
                    .userId(requestDocument.getCreatedBy())
                    .isRead(false)
                    .createdAt(OffsetDateTime.now())
                    .build();
            userNotificationRepository.save(reviewUserNotif);
        } else {
            // Update the existing UserNotification's createdAt to reflect the latest action
            UserNotification existingUserNotif = userNotificationRepository.findByUserIdAndNotification_NotificationId(requestDocument.getCreatedBy(), savedReviewNotif.getNotificationId());
            if (existingUserNotif != null) {
                existingUserNotif.setCreatedAt(OffsetDateTime.now());
                userNotificationRepository.save(existingUserNotif);
            }
        }

        auditService.record("UPLOAD_CERTIFICATES", requestId, actionLabel, reviewerId, request.getReviewNote());

        return CertificateResponse.builder()
                .userCertificateId(null)
                .issuer(requestDocument.getDocumentName())
                .certificatesUrl(requestDocument.getDocumentUrl())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public CertificateRequestDetailResponse getCertificateRequestDetail(Long requestId) {
        Long currentUserId = securityUtils.getCurrentUserId();

        RequestDocument requestDocument = requestDocumentRepository.findById(requestId)
                .orElseThrow(() -> new InvalidCredentialsException("Request document not found"));

        Notification notification = requestDocument.getNotification();

        if (!"UPLOAD_CERTIFICATES".equals(notification.getType())) {
            throw new InvalidCredentialsException("Invalid notification type");
        }

        // Only the owner of the request may view its detail
        if (!requestDocument.getCreatedBy().equals(currentUserId)) {
            throw new CustomAccesDeniedExceptions("Access denied: you can only view your own certificate requests");
        }

        return CertificateRequestDetailResponse.builder()
                .requestDetails(CertificateRequestDetailResponse.RequestDetails.builder()
                        .requestId(requestDocument.getDocumentId())
                        .certificateName(requestDocument.getDocumentName())
                        .certificatesUrl(requestDocument.getDocumentUrl())
                        .fileSize(requestDocument.getFileSize())
                        .submittedAt(requestDocument.getCreatedAt())
                        .submittedBy(resolveUserName(requestDocument.getCreatedBy()))
                        .build())
                .reviewInformation(CertificateRequestDetailResponse.ReviewInformation.builder()
                        .status(requestDocument.getStatus())
                        .reviewedAt(requestDocument.getUploadedAt())
                        .reviewNote(null) // assuming no review note stored
                        .reviewedBy(requestDocument.getUploadedAt() != null ? resolveUserName(notification.getActorId()) : null)
                        .build())
                .build();
    }

    private String resolveUserName(Long userId) {
        if (userId == null) return null;
        return userRepository.findByUserId(userId).map(User::getFirstName).orElse(null);
    }
}
