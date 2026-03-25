package com.example.skripsi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recruitment_steps")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RecruitmentStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruitment_step_id")
    private Long recruitmentStepId;

    @Column(name = "internship_header_id", nullable = false)
    private Long internshipHeaderId;

    @Column(name = "step_name", nullable = false, length = 100)
    private String stepName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_header_id", insertable = false, updatable = false)
    private InternshipHeader internshipHeader;
}
