package com.example.skripsi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "company_profiles")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CompanyProfile {
    @Id
    @Column(name = "company_profile_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyProfileId;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "bio")
    private String bio;

    @Column(name = "website")
    private String website;

    @Column(name = "is_partner")
    private Boolean isPartner;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;
}

