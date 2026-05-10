package com.example.skripsi.controllers;

import com.example.skripsi.models.*;
import com.example.skripsi.models.category.*;
import com.example.skripsi.services.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/category")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public WebResponse<?> addCategory(@Valid @RequestBody CreateCategoryRequest request) {
        var result = categoryService.addCategory(request);
        return WebResponse.builder()
                .success(true)
                .message("Successfully Add Category")
                .result(result)
                .build();
    }

    @PatchMapping("/category/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public WebResponse<?> updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request) {
        var result = categoryService.updateCategory(id, request);
        return WebResponse.builder()
                .success(true)
                .message("Successfully Update Category")
                .result(result)
                .build();
    }

    @PostMapping("/subcategory")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public WebResponse<?> addSubCategory(@Valid @RequestBody CreateSubCategoryRequest request) {
        var result = categoryService.addSubCategory(request);
        return WebResponse.builder()
                .success(true)
                .message("Successfully Add SubCategory")
                .result(result)
                .build();
    }

    @PatchMapping("/subcategory/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public WebResponse<?> updateSubCategory(@PathVariable Long id, @Valid @RequestBody UpdateSubCategoryRequest request) {
        var result = categoryService.updateSubCategory(id, request);
        return WebResponse.builder()
                .success(true)
                .message("Successfully Update SubCategory")
                .result(result)
                .build();
    }
}
