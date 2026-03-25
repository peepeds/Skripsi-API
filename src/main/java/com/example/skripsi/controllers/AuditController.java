package com.example.skripsi.controllers;

import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.*;
import com.example.skripsi.securities.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit")
public class AuditController {

    private final IAuditService auditService;
    private final SecurityUtils securityUtils;

    public AuditController(IAuditService auditService, SecurityUtils securityUtils) {
        this.auditService = auditService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public WebResponse<?> getAuditLogs(
            @RequestParam("entity") String entity,
            @RequestParam("id") Long id) {
        Long currentUserId = securityUtils.getCurrentUserId();
        var logs = auditService.getAuditLogs(entity, id, currentUserId);
        return WebResponse.builder()
                .success(true)
                .message("Audit log fetched")
                .result(logs)
                .build();
    }
}

