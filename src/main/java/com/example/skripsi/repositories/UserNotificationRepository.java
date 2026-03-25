package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    Page<UserNotification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByUserIdAndNotification_NotificationId(Long userId, Long notificationId);

    UserNotification findByUserIdAndNotification_NotificationId(Long userId, Long notificationId);
}
