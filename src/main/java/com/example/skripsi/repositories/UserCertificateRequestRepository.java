package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserCertificateRequestRepository extends JpaRepository<UserCertificateRequest, Long> {
    List<UserCertificateRequest> findByNotification_NotificationId(Long notificationId);
}
