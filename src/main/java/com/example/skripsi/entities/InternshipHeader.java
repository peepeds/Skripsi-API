package com.example.skripsi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "internship_headers")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class InternshipHeader {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internship_header_id")
    private Long internshipHeaderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "start_year")
    private LocalDate startYear;

    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(name = "job_title", length = 75)
    private String jobTitle;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    @OneToOne(mappedBy = "internshipHeader", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private InternshipDetail internshipDetail;

    @OneToMany(mappedBy = "internshipHeader", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RecruitmentStep> recruitmentSteps;

    @OneToMany(mappedBy = "internshipHeader", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<InternshipJobSubCategory> jobSubCategories;
}
