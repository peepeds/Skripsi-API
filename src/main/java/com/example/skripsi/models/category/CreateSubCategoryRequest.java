package com.example.skripsi.models.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateSubCategoryRequest {

    @NotBlank
    private String subCategoryName;

    @NotNull
    private Long categoryId;
}
