package com.example.skripsi.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "company_requests")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CompanyRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_request_id")
    private Long companyRequestId;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "company_abbreviation")
    private String companyAbbreviation;

    @Column(name = "website")
    private String website;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "is_partner")
    private Boolean isPartner;

    @Column(name = "subcategory_id")
    private Long subcategoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CompanyRequestStatus status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "review_note")
    private String reviewNote;
}
