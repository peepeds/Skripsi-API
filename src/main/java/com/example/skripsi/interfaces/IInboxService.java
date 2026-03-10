package com.example.skripsi.interfaces;
import com.example.skripsi.models.PageResponse;
import com.example.skripsi.models.inbox.InboxPreviewResponse;
public interface IInboxService {
    PageResponse<InboxPreviewResponse> getUserInboxPreview(int page, int limit);
}
