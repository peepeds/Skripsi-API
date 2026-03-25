package com.example.skripsi.interfaces;

import com.example.skripsi.models.*;
import com.example.skripsi.models.inbox.*;
public interface IInboxService {
    PageResponse<InboxPreviewResponse> getUserInboxPreview(int page, int limit);
}
