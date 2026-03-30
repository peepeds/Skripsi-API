package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.BadRequestExceptions;
import com.example.skripsi.models.category.*;
import com.example.skripsi.models.company.*;
import com.example.skripsi.models.*;
import com.example.skripsi.repositories.*;
import com.example.skripsi.interfaces.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService implements ICategoryService {
    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;
    private final SubCategoryRepository subCategoryRepository;

    public CategoryService(CategoryRepository categoryRepository,
                          CompanyRepository companyRepository,
                          SubCategoryRepository subCategoryRepository) {
        this.categoryRepository = categoryRepository;
        this.companyRepository = companyRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    public List<Category> getCategories(boolean includeSubCategories) {
        if (includeSubCategories) {
            return categoryRepository.findAllWithSubCategories();
        } else {
            return categoryRepository.findAll();
        }
    }

    public List<Category> getCategories(boolean includeSubCategories, String categoryType) {
        if (includeSubCategories) {
            return categoryRepository.findByCategoryTypeWithSubCategories(categoryType);
        } else {
            return categoryRepository.findByCategoryType(categoryType);
        }
    }

    public List<CategoryResponse> getCategoryResponse(boolean includeSubCategories) {
        List<Category> categories = getCategories(includeSubCategories);
        return categories.stream().map(category -> toResponse(category, includeSubCategories)).collect(Collectors.toList());
    }

    public List<CategoryResponse> getCategoryResponse(boolean includeSubCategories, String categoryType) {
        List<Category> categories = getCategories(includeSubCategories, categoryType);
        return categories.stream().map(category -> toResponse(category, includeSubCategories)).collect(Collectors.toList());
    }

    public CategoryResponse toResponse(Category category, boolean includeSubCategories) {
        List<SubCategoryResponse> subCategoryResponses = null;
        if (includeSubCategories && category.getSubCategories() != null) {
            subCategoryResponses = category.getSubCategories().stream()
                    .map(sub -> SubCategoryResponse.builder()
                            .subCategoryId(sub.getSubCategoryId())
                            .subCategoryName(sub.getSubCategoryName())
                            .build())
                    .collect(Collectors.toList());
        }
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .categoryType(category.getCategoryType())
                .subCategories(subCategoryResponses)
                .build();
    }

    public PageResponse<CompanyOptionsResponse> getCompaniesBySubCategory(Long subCategoryId, int page, int limit) {
        final int MAX_TOTAL_ELEMENTS = 1000;
        int requestedOffset = page * limit;
        if (requestedOffset >= MAX_TOTAL_ELEMENTS) {
            throw new RuntimeException("limit exceeded");
        }
        Pageable pageable = PageRequest.of(page, limit);
        Page<CompanyOptionsResponse> pageResult = companyRepository.findCompaniesBySubCategoryId(subCategoryId, pageable);
        long cappedTotalElements = Math.min(pageResult.getTotalElements(), MAX_TOTAL_ELEMENTS);
        return PageResponse.<CompanyOptionsResponse>builder()
                .result(pageResult.getContent())
                .meta(PageResponse.Meta.builder()
                        .page(page).size(limit)
                        .totalElements(cappedTotalElements)
                        .totalPages(calculateTotalPages(cappedTotalElements, limit))
                        .hasNext(hasNextPage(page, limit, cappedTotalElements))
                        .hasPrevious(page > 0)
                        .build())
                .build();
    }

    private int calculateTotalPages(long totalElements, int limit) {
        return (int) Math.ceil((double) totalElements / limit);
    }

    private boolean hasNextPage(int page, int limit, long totalElements) {
        return (long) (page + 1) * limit < totalElements;
    }

    public PageResponse<CompanyOptionsResponse> getCompaniesBySubCategoryName(
            String subCategoryName, String type, int page, int limit) {
        final int MAX_TOTAL_ELEMENTS = 1000;
        int requestedOffset = page * limit;
        if (requestedOffset >= MAX_TOTAL_ELEMENTS) {
            throw new RuntimeException("limit exceeded");
        }

        String normalizedType = type == null ? "" : type.trim().toLowerCase();
        if (!"companies".equals(normalizedType) && !"jobs".equals(normalizedType)) {
            throw new BadRequestExceptions("Invalid type. Allowed values: companies, jobs");
        }

        Pageable pageable = PageRequest.of(page, limit);
        Page<CompanyOptionsResponse> pageResult;

        if ("companies".equals(normalizedType)) {
            pageResult = companyRepository.findCompaniesBySubCategoryNameViaProfile(subCategoryName, pageable);
        } else {
            var subCategory = subCategoryRepository.findBySubCategoryNameIgnoreCase(subCategoryName);
            if (subCategory.isEmpty()) {
                return emptyPageResponse(page, limit);
            }
            SubCategory sub = subCategory.get();
            if (sub.getCategory() == null || !"jobs".equalsIgnoreCase(sub.getCategory().getCategoryType())) {
                return emptyPageResponse(page, limit);
            }
            pageResult = companyRepository.findCompaniesBySubCategoryIdViaProfile(sub.getSubCategoryId(), pageable);
        }

        long cappedTotalElements = Math.min(pageResult.getTotalElements(), MAX_TOTAL_ELEMENTS);
        return PageResponse.<CompanyOptionsResponse>builder()
                .result(pageResult.getContent())
                .meta(PageResponse.Meta.builder()
                        .page(page).size(limit)
                        .totalElements(cappedTotalElements)
                        .totalPages(calculateTotalPages(cappedTotalElements, limit))
                        .hasNext(hasNextPage(page, limit, cappedTotalElements))
                        .hasPrevious(page > 0)
                        .build())
                .build();
    }

    private PageResponse<CompanyOptionsResponse> emptyPageResponse(int page, int limit) {
        return PageResponse.<CompanyOptionsResponse>builder()
                .result(List.of())
                .meta(PageResponse.Meta.builder()
                        .page(page).size(limit)
                        .totalElements(0)
                        .totalPages(0)
                        .hasNext(false)
                        .hasPrevious(false)
                        .build())
                .build();
    }
}
