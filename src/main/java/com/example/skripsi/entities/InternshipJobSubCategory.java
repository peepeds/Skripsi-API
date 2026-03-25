package com.example.skripsi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "internship_job_subcategories")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class InternshipJobSubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internship_job_subcategory_id")
    private Long internshipJobSubCategoryId;

    @Column(name = "internship_header_id", nullable = false)
    private Long internshipHeaderId;

    @Column(name = "sub_category_id", nullable = false)
    private Long subCategoryId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_header_id", insertable = false, updatable = false)
    private InternshipHeader internshipHeader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id", insertable = false, updatable = false)
    private SubCategory subCategory;
}
