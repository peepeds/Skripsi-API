package com.example.skripsi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @JsonProperty("categoryId")
    public Long getCategoryId() {
        return category != null ? category.getCategoryId() : null;
    }

    @JsonProperty("categoryId")
    public void setCategoryId(Long categoryId) {
        if (categoryId == null) {
            this.category = null;
            return;
        }

        Category category = new Category();
        category.setCategoryId(categoryId);
        this.category = category;
    }
}

