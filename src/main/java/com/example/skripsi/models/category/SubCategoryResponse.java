package com.example.skripsi.models.category;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubCategoryResponse {
    private Long subCategoryId;
    private String subCategoryName;
}

