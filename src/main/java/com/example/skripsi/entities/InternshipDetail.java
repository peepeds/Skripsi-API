package com.example.skripsi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "internship_details")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class InternshipDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internship_detail_id")
    private Long internshipDetailId;

    @Column(name = "internship_header_id", nullable = false, unique = true)
    private Long internshipHeaderId;

    @Column(name = "type", length = 10)
    private String type;

    @Column(name = "scheme", length = 10)
    private String scheme;

    @Column(name = "work_culture_rating")
    private Integer workCultureRating;

    @Column(name = "work_life_balance_rating")
    private Integer workLifeBalanceRating;

    @Column(name = "learning_opportunity_rating")
    private Integer learningOpportunityRating;

    @Column(name = "mentorship_rating")
    private Integer mentorshipRating;

    @Column(name = "benefits_rating")
    private Integer benefitsRating;

    @Column(name = "interview_difficulty_rating")
    private Integer interviewDifficultyRating;

    @Column(name = "testimony", length = 500)
    private String testimony;

    @Column(name = "pros", length = 500)
    private String pros;

    @Column(name = "cons", length = 500)
    private String cons;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_header_id", insertable = false, updatable = false)
    private InternshipHeader internshipHeader;
}
