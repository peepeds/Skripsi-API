package com.example.skripsi.controllers;

import com.example.skripsi.models.*;
import com.example.skripsi.services.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/category")
    public WebResponse<?> getCategories(
            @RequestParam(value = "IncludeSubCategories", defaultValue = "1") int includeSubCategories,
            @RequestParam(value = "type", defaultValue = "jobs") String categoryType) {
        boolean include = includeSubCategories == 1;
        var result = categoryService.getCategoryResponse(include, categoryType);
        return WebResponse.builder()
                .success(true)
                .message("Successfully Get Categories data")
                .result(result)
                .build();
    }

    @GetMapping("/subcategory/{subCategoryId}/companies")
    public WebResponse<?> getCompaniesBySubCategory(
            @PathVariable("subCategoryId") Long subCategoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        var result = categoryService.getCompaniesBySubCategory(subCategoryId, page, limit);
        return WebResponse.builder()
                .success(true)
                .message("Successfully Get Companies by SubCategory data")
                .result(result.getResult())
                .meta(result.getMeta())
                .build();
    }
}
