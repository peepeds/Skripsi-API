package com.example.skripsi.models.category;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCategoryRequest {

    private String categoryName;

    private String categoryType;
}
