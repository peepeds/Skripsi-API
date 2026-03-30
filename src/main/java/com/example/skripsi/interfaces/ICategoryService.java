package com.example.skripsi.interfaces;

import com.example.skripsi.entities.Category;
import com.example.skripsi.models.category.CategoryResponse;
import com.example.skripsi.models.company.CompanyOptionsResponse;
import com.example.skripsi.models.PageResponse;

import java.util.List;

public interface ICategoryService {
    List<Category> getCategories(boolean includeSubCategories);

    List<Category> getCategories(boolean includeSubCategories, String categoryType);

    List<CategoryResponse> getCategoryResponse(boolean includeSubCategories);

    List<CategoryResponse> getCategoryResponse(boolean includeSubCategories, String categoryType);

    CategoryResponse toResponse(Category category, boolean includeSubCategories);

    PageResponse<CompanyOptionsResponse> getCompaniesBySubCategory(Long subCategoryId, int page, int limit);
}
