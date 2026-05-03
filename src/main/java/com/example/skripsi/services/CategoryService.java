package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.BadRequestExceptions;
import com.example.skripsi.exceptions.ResourceNotFoundException;
import com.example.skripsi.models.category.*;
import com.example.skripsi.models.company.*;
import com.example.skripsi.models.*;
import com.example.skripsi.models.constant.*;
import com.example.skripsi.repositories.*;
import com.example.skripsi.repositories.projections.SubCategorySummaryProjection;
import com.example.skripsi.interfaces.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryService implements ICategoryService {
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ICompanyService companyService;
    private final CompanyProfileRepository companyProfileRepository;
    private final InternshipDetailRepository internshipDetailRepository;

    public CategoryService(CategoryRepository categoryRepository,
                          SubCategoryRepository subCategoryRepository,
                          @Lazy ICompanyService companyService,
                          CompanyProfileRepository companyProfileRepository,
                          InternshipDetailRepository internshipDetailRepository) {
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.companyService = companyService;
        this.companyProfileRepository = companyProfileRepository;
        this.internshipDetailRepository = internshipDetailRepository;
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

    public CursorPageResponse<CompanyOptionsResponse> getCompaniesBySubCategory(Long subCategoryId, Long cursor, int limit) {
        return companyService.getCompaniesBySubCategoryId(subCategoryId, cursor, limit);
    }

    public CursorPageResponse<CompanyOptionsResponse> getCompaniesBySubCategoryName(
            String subCategoryName, String type, Long cursor, int limit) {
        String normalizedType = type == null ? "" : type.trim().toLowerCase();

        if (!TypeConstants.COMPANIES.equals(normalizedType) && !TypeConstants.JOBS.equals(normalizedType)) {
            throw new BadRequestExceptions(MessageConstants.Validation.INVALID_TYPE_COMPANIES_OR_JOBS);
        }

        if (TypeConstants.COMPANIES.equals(normalizedType)) {
            return companyService.getCompaniesBySubCategoryNameViaProfile(subCategoryName, cursor, limit);
        } else {
            var subCategory = subCategoryRepository.findBySubCategoryNameIgnoreCase(subCategoryName);

            if (subCategory.isEmpty()) {
                return emptyCursorPageResponse();
            }

            SubCategory sub = subCategory.get();

            return companyService.getCompaniesBySubCategoryId(sub.getSubCategoryId(), cursor, limit);
        }
    }

    @Override
    public boolean existsSubCategoryById(Long id) {
        return subCategoryRepository.existsById(id);
    }

    @Override
    public Map<Long, String> getSubCategoryNameMap(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return subCategoryRepository.findBySubCategoryIds(ids).stream()
                .collect(Collectors.toMap(
                        SubCategory::getSubCategoryId,
                        SubCategory::getSubCategoryName
                ));
    }

    @Override
    public List<TopSubCategoryResponse> getTopSubCategories() {
        return subCategoryRepository.findTop10SubCategoryNames().stream()
                .map(projection -> TopSubCategoryResponse.builder()
                        .subcategoryName(projection.getSubCategoryName())
                        .url("/" + URLEncoder.encode(projection.getSubCategoryName(), StandardCharsets.UTF_8).replace("+", "%20") + "/companies")
                        .totalReviews(projection.getTotalReviews())
                        .build())
                .collect(Collectors.toList());
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

    private CursorPageResponse<CompanyOptionsResponse> emptyCursorPageResponse() {
        return CursorPageResponse.<CompanyOptionsResponse>builder()
                .result(List.of())
                .meta(CursorPageResponse.Meta.builder()
                        .nextCursor(null)
                        .previousCursor(null)
                        .size(0)
                        .hasMore(false)
                        .build())
                .build();
    }

    @Override
    public SubCategorySummaryResponse getSubCategorySummary(String subCategoryName) {
        SubCategory subCategory = subCategoryRepository.findBySubCategoryNameIgnoreCase(subCategoryName)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found: " + subCategoryName));

        Long subCategoryId = subCategory.getSubCategoryId();
        String categoryType = subCategory.getCategory().getCategoryType();

        Long totalCompanies;
        Long totalPartnerCompanies;
        SubCategorySummaryProjection summary;

        if (TypeConstants.JOBS.equals(categoryType)) {
            totalCompanies = companyProfileRepository.countCompaniesByJobSubcategoryId(subCategoryId);
            totalPartnerCompanies = companyProfileRepository.countPartnerCompaniesByJobSubcategoryId(subCategoryId);
            summary = internshipDetailRepository.findSummaryByJobSubCategoryId(subCategoryId);
        } else {
            totalCompanies = companyProfileRepository.countBySubcategoryId(subCategoryId);
            totalPartnerCompanies = companyProfileRepository.countPartnersBySubcategoryId(subCategoryId);
            summary = internshipDetailRepository.findSummaryBySubCategoryId(subCategoryId);
        }

        return SubCategorySummaryResponse.builder()
                .totalCompanies(totalCompanies)
                .totalReviews(summary != null ? summary.getTotalReviews() : 0L)
                .avgRating(summary != null ? summary.getAvgRating() : null)
                .totalPartnerCompanies(totalPartnerCompanies)
                .build();
    }

}
