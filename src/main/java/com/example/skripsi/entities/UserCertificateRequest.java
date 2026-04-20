package com.example.skripsi.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_certificate_requests")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserCertificateRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id")
    private Notification notification;

    @Column(name = "document_name")
    private String documentName;

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "issuer")
    private Long issuer;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "uploaded_at")
    private OffsetDateTime uploadedAt;
}
