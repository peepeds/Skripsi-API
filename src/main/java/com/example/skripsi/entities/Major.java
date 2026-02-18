package com.example.skripsi.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "majors")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Major {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", referencedColumnName = "region_id",nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", referencedColumnName = "dept_id",nullable = false)
    private Department department;

    @Column(name = "major_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer majorId;

    @Column(name = "major_name")
    private String majorName;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "is_active")
    private Boolean active;
}
