package com.example.skripsi.models.category;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSubCategoryRequest {

    private String subCategoryName;

    private Long categoryId;
}
