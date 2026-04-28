package com.example.skripsi.interfaces;

import com.example.skripsi.entities.Notification;
import com.example.skripsi.entities.User;
import com.example.skripsi.entities.UserCertificateRequest;
import com.example.skripsi.models.user.*;

import com.example.skripsi.models.CursorPageResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUserService {
    List<UserResponse> getAllUserByUserPrivilege();
    Boolean emailExists(String email);
    UserResponse getUserProfile();
    String resolveUserName(Long userId);
    Map<Long, String> getUserNameMap(List<Long> userIds);
    Boolean userExists(Long userId);
    Optional<User> findUserById(Long userId);
    List<Notification> findNotificationsByTypeAndReferenceId(String type, Long referenceId);
    List<UserCertificateRequest> findUserCertificateRequestsByNotificationId(Long notificationId);
    Boolean isCertificateRequestOwner(Long requestId, Long userId);
    List<CertificateResponse> getMyCertificates();
    CursorPageResponse<CertificateRequestListResponse> getCertificateRequests(String status, Long cursor, int limit);
}
