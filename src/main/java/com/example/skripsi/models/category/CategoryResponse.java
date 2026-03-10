package com.example.skripsi.models.category;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CategoryResponse {
    private Long categoryId;
    private String categoryName;
    private List<SubCategoryResponse> subCategories;
}

