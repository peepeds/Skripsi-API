package com.example.skripsi.models.inbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InboxPreviewResponse {
    private Long inboxId;
    private String type;
    private String action;
    private String activity;
    private Long referenceId;
    private String referenceUrl;
    private boolean isRead;
    private OffsetDateTime createdAt;
}
