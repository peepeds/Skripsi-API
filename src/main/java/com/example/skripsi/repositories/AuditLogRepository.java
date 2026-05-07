package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampAsc(String entityType, Long entityId);

    Optional<AuditLog> findTopByEntityTypeAndEntityIdAndActionInOrderByTimestampDesc(
            String entityType, Long entityId, List<String> actions);
}

