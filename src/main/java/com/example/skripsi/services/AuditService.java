package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.audit.*;
import com.example.skripsi.models.constant.*;
import com.example.skripsi.repositories.AuditLogRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuditService implements IAuditService {

    private static final List<String> VALID_ENTITIES = List.of(EntityTypeConstants.COMPANY_REQUEST, EntityTypeConstants.UPLOAD_CERTIFICATES);

    private final AuditLogRepository auditLogRepository;
    private final ICompanyService companyService;
    private final IUserService userService;

    public AuditService(AuditLogRepository auditLogRepository, ICompanyService companyService, IUserService userService) {
        this.auditLogRepository = auditLogRepository;
        this.companyService = companyService;
        this.userService = userService;
    }

    @Override
    public List<AuditLogResponse> getAuditLogs(String entity, Long id, Long currentUserId) {
        if (entity == null || id == null) {
            throw new BadRequestExceptions(MessageConstants.Validation.ENTITY_AND_ID_REQUIRED);
        }

        String entityUpper = entity.toUpperCase();

        if (!VALID_ENTITIES.contains(entityUpper)) {
            throw new BadRequestExceptions(MessageConstants.Validation.UNKNOWN_ENTITY_TYPE + entity);
        }

        if (EntityTypeConstants.COMPANY_REQUEST.equals(entityUpper)) {
            Boolean isOwner = companyService.isCompanyRequestOwner(id, currentUserId);

            if (!isOwner) {
                throw new CustomAccessDeniedException(MessageConstants.NotFound.ACCESS_DENIED_OWN_AUDIT_LOGS);
            }
        }

        if (EntityTypeConstants.UPLOAD_CERTIFICATES.equals(entityUpper)) {
            Boolean isOwner = userService.isCertificateRequestOwner(id, currentUserId);

            if (!isOwner) {
                throw new CustomAccessDeniedException(MessageConstants.NotFound.ACCESS_DENIED_OWN_AUDIT_LOGS);
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
        return userService.resolveUserName(userId);
    }
}
