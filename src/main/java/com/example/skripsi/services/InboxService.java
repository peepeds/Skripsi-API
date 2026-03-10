package com.example.skripsi.services;

import com.example.skripsi.entities.CompanyRequest;
import com.example.skripsi.entities.UserNotification;
import com.example.skripsi.exceptions.BadRequestExceptions;
import com.example.skripsi.interfaces.IInboxService;
import com.example.skripsi.models.PageResponse;
import com.example.skripsi.models.inbox.InboxPreviewResponse;
import com.example.skripsi.repositories.CompanyRequestRepository;
import com.example.skripsi.repositories.UserNotificationRepository;
import com.example.skripsi.securities.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Transactional
public class InboxService implements IInboxService {

    private static final Map<String, String> REFERENCE_URL_TEMPLATES = Map.of(
            "COMPANY_REQUEST", "/company/request/%d",
            "UPLOAD_CERTIFICATES", "/user/certificate/request/%d"
    );

    private final UserNotificationRepository userNotificationRepository;
    private final CompanyRequestRepository companyRequestRepository;
    private final SecurityUtils securityUtils;

    public InboxService(UserNotificationRepository userNotificationRepository,
                        CompanyRequestRepository companyRequestRepository,
                        SecurityUtils securityUtils) {
        this.userNotificationRepository = userNotificationRepository;
        this.companyRequestRepository = companyRequestRepository;
        this.securityUtils = securityUtils;
    }

    @Override
    public PageResponse<InboxPreviewResponse> getUserInboxPreview(int page, int limit) {
        Long userId = securityUtils.getCurrentUserId();
        final int MAX_TOTAL_ELEMENTS = 1000;

        int requestedOffset = page * limit;
        if (requestedOffset >= MAX_TOTAL_ELEMENTS) {
            throw new BadRequestExceptions("limit exceeded");
        }

        Pageable pageable = PageRequest.of(page, limit);
        Page<InboxPreviewResponse> pageResult = userNotificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toPreviewResponse);

        long actualTotalElements = pageResult.getTotalElements();
        long cappedTotalElements = Math.min(actualTotalElements, MAX_TOTAL_ELEMENTS);

        int totalPages = calculateTotalPages(cappedTotalElements, limit);
        boolean hasNext = hasNextPage(page, limit, cappedTotalElements);

        return PageResponse.<InboxPreviewResponse>builder()
                .result(pageResult.getContent())
                .meta(PageResponse.Meta.builder()
                        .page(page)
                        .size(limit)
                        .totalElements(cappedTotalElements)
                        .totalPages(totalPages)
                        .hasNext(hasNext)
                        .hasPrevious(page > 0)
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
        if ("COMPANY_REQUEST".equals(type)) {
            return companyRequestRepository.findById(referenceId)
                    .map(CompanyRequest::getCompanyName)
                    .orElse("Unknown");
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

    private int calculateTotalPages(long totalElements, int limit) {
        return (int) Math.ceil((double) totalElements / limit);
    }

    private boolean hasNextPage(int page, int limit, long totalElements) {
        return (long) (page + 1) * limit < totalElements;
    }
}
