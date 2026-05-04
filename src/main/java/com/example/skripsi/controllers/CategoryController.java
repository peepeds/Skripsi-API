package com.example.skripsi.controllers;

import com.example.skripsi.models.*;
import com.example.skripsi.models.category.*;
import com.example.skripsi.services.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/category")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        var result = categoryService.createCategoryMasterData(request);
        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "OK", "result", result));
    }

    @PatchMapping("/category/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> updateCategory(@PathVariable Integer id, @Valid @RequestBody UpdateCategoryRequest request) {
        var result = categoryService.updateCategoryMasterData(id, request);
        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "OK", "result", result));
    }

    @DeleteMapping("/category/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> deleteCategory(@PathVariable Integer id, @RequestBody(required = false) java.util.Map<String, Object> body) {
        var result = categoryService.deleteCategoryMasterData(id, body);
        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "OK", "result", result));
    }

    @PostMapping("/subcategory")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> createSubCategory(@RequestBody java.util.Map<String, Object> body) {
        var result = categoryService.createSubCategoryMasterData(body);
        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "OK", "result", result));
    }

    @PatchMapping("/subcategory/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> updateSubCategory(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        var result = categoryService.updateSubCategoryMasterData(id, body);
        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "OK", "result", result));
    }

    @DeleteMapping("/subcategory/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> deleteSubCategory(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, Object> body) {
        var result = categoryService.deleteSubCategoryMasterData(id, body);
        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "OK", "result", result));
    }

    @GetMapping("/category")
    public ResponseEntity<java.util.Map<String, Object>> getCategories(
            @RequestParam(value = "IncludeSubCategories", defaultValue = "1") int includeSubCategories,
            @RequestParam(value = "type", defaultValue = "jobs") String categoryType) {
        boolean include = includeSubCategories == 1;
        var result = categoryService.getCategoryResponse(include, categoryType);
        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "OK", "result", result));
    }

    @GetMapping("/subcategory/{subCategoryName}/companies")
    public ResponseEntity<java.util.Map<String, Object>> getCompaniesBySubCategory(
            @PathVariable("subCategoryName") String subCategoryName,
            @RequestParam(value = "type", defaultValue = "companies") String type,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        var result = categoryService.getCompaniesBySubCategoryName(subCategoryName, type, cursor, limit);
        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "OK", "result", result));
    }

    @GetMapping("/top-categories")
    public ResponseEntity<java.util.Map<String, Object>> getTopCategories() {
        var result = categoryService.getTopSubCategories();
        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "OK", "result", result));
    }

    @GetMapping("/subcategory/{subCategoryName}/summary")
    public ResponseEntity<java.util.Map<String, Object>> getSubCategorySummary(@PathVariable("subCategoryName") String subCategoryName) {
        String decoded = URLDecoder.decode(subCategoryName, StandardCharsets.UTF_8);
        var result = categoryService.getSubCategorySummary(decoded);
        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "OK", "result", result));
    }

    @GetMapping("/subcategory")
    public ResponseEntity<java.util.Map<String, Object>> getAllSubCategories() {
        var result = categoryService.getAllSubCategories();
        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "OK", "result", result));
    }

}
