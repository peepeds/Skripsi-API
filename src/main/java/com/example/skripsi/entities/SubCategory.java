package com.example.skripsi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sub_categories")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_category_id")
    private Long subCategoryId;

    @Column(name = "sub_category_name")
    private String subCategoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}

