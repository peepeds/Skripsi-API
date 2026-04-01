package com.example.skripsi.controllers;

import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inbox")
public class InboxController {

    private final IInboxService inboxService;

    public InboxController(IInboxService inboxService) {
        this.inboxService = inboxService;
    }

    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public WebResponse<?> getInboxPreview(
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        var results = inboxService.getUserInboxPreview(cursor, limit);
        return WebResponse.builder()
                .success(true)
                .message("Inbox fetched")
                .meta(results.getMeta())
                .result(results.getResult())
                .build();
    }
}

