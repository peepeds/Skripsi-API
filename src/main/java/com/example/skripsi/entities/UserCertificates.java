package com.example.skripsi.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_certificates")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserCertificates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_certificate_id")
    private Long userCertificateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", referencedColumnName = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @Column(name = "issuer")
    private Long issuer;

    @Column(name = "certificates_url")
    private String certificatesUrl;

    @Column(name = "certificate_name")
    private String certificateName;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
