package com.example.skripsi.utilities;

import com.example.skripsi.entities.Notification;
import com.example.skripsi.entities.UserNotification;
import com.example.skripsi.repositories.NotificationRepository;
import com.example.skripsi.repositories.UserNotificationRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class NotificationHelper {

    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;

    public NotificationHelper(NotificationRepository notificationRepository,
                              UserNotificationRepository userNotificationRepository) {
        this.notificationRepository = notificationRepository;
        this.userNotificationRepository = userNotificationRepository;
    }

    public Notification createNotification(String type, String action, Long referenceId, Long actorId) {
        Notification notification = Notification.builder()
                .type(type)
                .action(action)
                .referenceId(referenceId)
                .actorId(actorId)
                .createdAt(OffsetDateTime.now())
                .build();
        return notificationRepository.save(notification);
    }

    public UserNotification createUserNotification(Notification notification, Long userId) {
        UserNotification userNotification = UserNotification.builder()
                .notification(notification)
                .userId(userId)
                .isRead(false)
                .createdAt(OffsetDateTime.now())
                .build();
        return userNotificationRepository.save(userNotification);
    }

    public void createNotificationWithUserNotification(String type, String action,
                                                       Long referenceId, Long actorId, Long userId) {
        Notification notification = createNotification(type, action, referenceId, actorId);
        createUserNotification(notification, userId);
    }

    public void updateNotificationAction(Notification notification, String newAction, Long reviewerId) {
        notification.setAction(newAction);
        notification.setActorId(reviewerId);
        notification.setCreatedAt(OffsetDateTime.now());
        notificationRepository.save(notification);
    }

    public void updateOrCreateUserNotification(Notification notification, Long userId) {
        boolean exists = userNotificationRepository.existsByUserIdAndNotification_NotificationId(
                userId, notification.getNotificationId());

        if (!exists) {
            createUserNotification(notification, userId);
        } else {
            UserNotification existingUserNotif = userNotificationRepository
                    .findByUserIdAndNotification_NotificationId(userId, notification.getNotificationId());
            if (existingUserNotif != null) {
                existingUserNotif.setCreatedAt(OffsetDateTime.now());
                userNotificationRepository.save(existingUserNotif);
            }
        }
    }
}
