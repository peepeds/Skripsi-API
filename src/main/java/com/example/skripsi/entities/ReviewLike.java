package com.example.skripsi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_likes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ReviewLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_like_id")
    private Long reviewLikeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "internship_header_id", nullable = false)
    private Long internshipHeaderId;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_header_id", insertable = false, updatable = false)
    private InternshipHeader internshipHeader;
}
