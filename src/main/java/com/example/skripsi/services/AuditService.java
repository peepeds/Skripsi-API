package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.audit.*;
import com.example.skripsi.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuditService implements IAuditService {

    private static final List<String> VALID_ENTITIES = List.of("COMPANY_REQUEST", "UPLOAD_CERTIFICATES");

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final CompanyRequestRepository companyRequestRepository;
    private final NotificationRepository notificationRepository;
    private final UserCertificateRequestRepository userCertificateRequestRepository;

    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository,
                        CompanyRequestRepository companyRequestRepository, NotificationRepository notificationRepository,
                        UserCertificateRequestRepository userCertificateRequestRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.companyRequestRepository = companyRequestRepository;
        this.notificationRepository = notificationRepository;
        this.userCertificateRequestRepository = userCertificateRequestRepository;
    }

    @Override
    public List<AuditLogResponse> getAuditLogs(String entity, Long id, Long currentUserId) {
        if (entity == null || id == null) {
            throw new BadRequestExceptions("entity and id are required");
        }
        String entityUpper = entity.toUpperCase();
        if (!VALID_ENTITIES.contains(entityUpper)) {
            throw new BadRequestExceptions("Unknown entity type: " + entity);
        }

        if ("COMPANY_REQUEST".equals(entityUpper)) {
            var request = companyRequestRepository.findById(id)
                    .orElseThrow(() -> new BadRequestExceptions("Company request not found"));
            if (!request.getCreatedBy().equals(currentUserId)) {
                throw new CustomAccesDeniedExceptions("Access denied: you can only view audit logs for your own requests");
            }
        }

        if ("UPLOAD_CERTIFICATES".equals(entityUpper)) {
            var userCertificateRequest = userCertificateRequestRepository.findById(id)
                    .orElseThrow(() -> new BadRequestExceptions("Request document not found"));
            if (!userCertificateRequest.getCreatedBy().equals(currentUserId)) {
                throw new CustomAccesDeniedExceptions("Access denied: you can only view audit logs for your own requests");
            }
        }

        List<AuditLog> logs = auditLogRepository
                .findByEntityTypeAndEntityIdOrderByTimestampAsc(entityUpper, id);

        return logs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void record(String entityType, Long entityId, String action, Long actorId) {
        record(entityType, entityId, action, actorId, null);
    }

    public void record(String entityType, Long entityId, String action, Long actorId, String notes) {
        AuditLog log = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .actorId(actorId)
                .timestamp(java.time.OffsetDateTime.now())
                .notes(notes)
                .build();
        auditLogRepository.save(log);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        String actorName = resolveUserName(log.getActorId());
        return AuditLogResponse.builder()
                .action(log.getAction())
                .actor(actorName)
                .timestamp(log.getTimestamp())
                .notes(log.getNotes())
                .build();
    }

    private String resolveUserName(Long userId) {
        if (userId == null) return null;
        return userRepository.findByUserId(userId)
                .map(u -> u.getFirstName() + (u.getLastName() != null ? " " + u.getLastName() : ""))
                .orElse(null);
    }
}
