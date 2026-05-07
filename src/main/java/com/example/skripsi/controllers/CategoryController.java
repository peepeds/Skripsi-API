package com.example.skripsi.controllers;

import com.example.skripsi.models.*;
import com.example.skripsi.services.*;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

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

    @GetMapping("/subcategory/{subCategoryName}/companies")
    public WebResponse<?> getCompaniesBySubCategory(
            @PathVariable("subCategoryName") String subCategoryName,
            @RequestParam(value = "type", defaultValue = "companies") String type,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        var result = categoryService.getCompaniesBySubCategoryName(subCategoryName, type, cursor, limit);
        return WebResponse.builder()
                .success(true)
                .message("Successfully Get Companies by SubCategory data")
                .result(result.getResult())
                .meta(result.getMeta())
                .build();
    }

    @GetMapping("/top-categories")
    public WebResponse<?> getTopCategories() {
        var result = categoryService.getTopSubCategories();
        return WebResponse.builder()
                .success(true)
                .message("Successfully Get Top Categories data")
                .result(result)
                .build();
    }

    @GetMapping("/subcategory/{subCategoryName}/summary")
    public WebResponse<?> getSubCategorySummary(@PathVariable("subCategoryName") String subCategoryName) {
        String decoded = URLDecoder.decode(subCategoryName, StandardCharsets.UTF_8);
        var result = categoryService.getSubCategorySummary(decoded);
        return WebResponse.builder()
                .success(true)
                .message("Successfully Get SubCategory Summary data")
                .result(result)
                .build();
    }
}
