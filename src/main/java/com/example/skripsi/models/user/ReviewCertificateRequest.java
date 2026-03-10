package com.example.skripsi.models.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCertificateRequest {
    private String status; // APPROVED or REJECTED
    private String reviewNote;
}
