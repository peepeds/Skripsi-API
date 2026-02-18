package com.example.skripsi.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_tokens")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserToken {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id",nullable = false)
    private User user;

    @Column(name = "token_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    @Column(name = "jti")
    private String jti;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "is_revoked")
    private Boolean revoked;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
