package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    Page<UserNotification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByUserIdAndNotification_NotificationId(Long userId, Long notificationId);

    UserNotification findByUserIdAndNotification_NotificationId(Long userId, Long notificationId);

    @Query("SELECT un FROM UserNotification un WHERE un.userId = :userId AND (:cursor IS NULL OR un.userNotificationId < :cursor) ORDER BY un.userNotificationId DESC")
    List<UserNotification> findPageByUserIdDesc(@Param("userId") Long userId, @Param("cursor") Long cursor, Pageable pageable);
}
