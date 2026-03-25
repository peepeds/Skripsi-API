package com.example.skripsi.interfaces;

import com.example.skripsi.models.audit.*;

import java.util.List;

public interface IAuditService {
    List<AuditLogResponse> getAuditLogs(String entity, Long id, Long currentUserId);
}

