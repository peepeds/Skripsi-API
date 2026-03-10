package com.example.skripsi.controllers;

import com.example.skripsi.models.WebResponse;
import com.example.skripsi.services.CategoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("category")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("")
    public WebResponse<?> getCategories(@RequestParam(value = "IncludeSubCategories", defaultValue = "1") int includeSubCategories) {
        boolean include = includeSubCategories == 1;
        var result = categoryService.getCategoryResponse(include);
        return WebResponse.builder()
                .success(true)
                .message("Successfully Get Categories data")
                .result(result)
                .build();
    }
}
