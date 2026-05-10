package com.example.skripsi.models.category;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryRequest {

    @NotBlank
    private String categoryName;

    @NotBlank
    private String categoryType;
}
