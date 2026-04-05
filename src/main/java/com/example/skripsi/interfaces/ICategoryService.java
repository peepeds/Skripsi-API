package com.example.skripsi.interfaces;

import com.example.skripsi.entities.Category;
import com.example.skripsi.models.category.CategoryResponse;
import com.example.skripsi.models.category.TopSubCategoryResponse;
import com.example.skripsi.models.company.CompanyOptionsResponse;
import com.example.skripsi.models.CursorPageResponse;

import java.util.List;
import java.util.Map;

public interface ICategoryService {
    List<Category> getCategories(boolean includeSubCategories);

    List<Category> getCategories(boolean includeSubCategories, String categoryType);

    List<CategoryResponse> getCategoryResponse(boolean includeSubCategories);

    List<CategoryResponse> getCategoryResponse(boolean includeSubCategories, String categoryType);

    CategoryResponse toResponse(Category category, boolean includeSubCategories);

    CursorPageResponse<CompanyOptionsResponse> getCompaniesBySubCategory(Long subCategoryId, Long cursor, int limit);

    CursorPageResponse<CompanyOptionsResponse> getCompaniesBySubCategoryName(String subCategoryName, String type, Long cursor, int limit);

    boolean existsSubCategoryById(Long id);

    Map<Long, String> getSubCategoryNameMap(List<Long> ids);

    List<TopSubCategoryResponse> getTopSubCategories();
}
