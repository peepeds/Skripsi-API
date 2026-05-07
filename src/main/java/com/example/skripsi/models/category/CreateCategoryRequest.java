package com.example.skripsi.models.category;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryRequest {
    @NotBlank
    private String categoryName;
    
    @NotBlank
    private String categoryType;
}
