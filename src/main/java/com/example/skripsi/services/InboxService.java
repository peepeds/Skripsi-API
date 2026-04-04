package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.*;
import com.example.skripsi.models.inbox.*;
import com.example.skripsi.models.constant.*;
import com.example.skripsi.repositories.*;
import com.example.skripsi.securities.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class InboxService implements IInboxService {

    private static final Map<String, String> REFERENCE_URL_TEMPLATES = Map.of(
            EntityTypeConstants.COMPANY_REQUEST, UrlPathConstants.COMPANY_REQUEST_PATH,
            EntityTypeConstants.UPLOAD_CERTIFICATES, UrlPathConstants.UPLOAD_CERTIFICATES_PATH
    );

    private final UserNotificationRepository userNotificationRepository;
    private final ICompanyService companyService;
    private final SecurityUtils securityUtils;

    public InboxService(UserNotificationRepository userNotificationRepository,
                        ICompanyService companyService,
                        SecurityUtils securityUtils) {
        this.userNotificationRepository = userNotificationRepository;
        this.companyService = companyService;
        this.securityUtils = securityUtils;
    }

    @Override
    public CursorPageResponse<InboxPreviewResponse> getUserInboxPreview(Long cursor, int limit) {
        Long userId = securityUtils.getCurrentUserId();

        Pageable pageable = PageRequest.of(0, limit + 1);
        List<UserNotification> notifications = userNotificationRepository
                .findPageByUserIdDesc(userId, cursor, pageable);

        boolean hasMore = notifications.size() > limit;
        List<UserNotification> pageNotifications = hasMore ? notifications.subList(0, limit) : notifications;

        List<InboxPreviewResponse> items = pageNotifications.stream()
                .map(this::toPreviewResponse)
                .collect(Collectors.toList());

        Long nextCursor = hasMore && !items.isEmpty()
                ? items.get(items.size() - 1).getInboxId()
                : null;

        return CursorPageResponse.<InboxPreviewResponse>builder()
                .result(items)
                .meta(CursorPageResponse.Meta.builder()
                        .nextCursor(nextCursor)
                        .previousCursor(cursor)
                        .size(items.size())
                        .hasMore(hasMore)
                        .build())
                .build();
    }

    private InboxPreviewResponse toPreviewResponse(UserNotification un) {
        var notif = un.getNotification();
        String activity = resolveActivity(notif.getType(), notif.getAction(), notif.getReferenceId());
        return InboxPreviewResponse.builder()
                .inboxId(un.getUserNotificationId())
                .type(notif.getType())
                .action(notif.getAction())
                .activity(activity)
                .referenceId(notif.getReferenceId())
                .referenceUrl(resolveReferenceUrl(notif.getType(), notif.getReferenceId()))
                .isRead(un.isRead())
                .createdAt(un.getCreatedAt())
                .build();
    }

    private String resolveActivity(String type, String action, Long referenceId) {
        if (type == null || action == null || referenceId == null) return null;

        if (EntityTypeConstants.COMPANY_REQUEST.equals(type)) {
            return companyService.getCompanyRequestName(referenceId);
        } else {
            return normalizeType(type);
        }
    }

    private String normalizeType(String type) {
        if (type == null) return null;
        String[] words = type.toLowerCase().replace('_', ' ').split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }

        return sb.toString().trim();
    }

    private String resolveReferenceUrl(String type, Long referenceId) {
        if (type == null || referenceId == null) return null;
        String template = REFERENCE_URL_TEMPLATES.get(type);
        return template != null ? template.formatted(referenceId) : null;
    }
}
