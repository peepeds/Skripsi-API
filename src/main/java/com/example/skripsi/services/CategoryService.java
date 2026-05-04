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
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
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
        String normalizedType = normalizeCategoryType(categoryType);
        if (includeSubCategories) {
            return categoryRepository.findByCategoryTypeWithSubCategories(normalizedType);
        } else {
            return categoryRepository.findByCategoryType(normalizedType);
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
        public Map<Long, String> getSubCategoryCategoryNameMap(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return subCategoryRepository.findBySubCategoryIds(ids).stream()
            .filter(sub -> sub.getCategory() != null)
            .collect(Collectors.toMap(
                SubCategory::getSubCategoryId,
                sub -> sub.getCategory().getCategoryName(),
                (existing, replacement) -> existing
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
                    .categoryId(category.getCategoryId())
                    .categoryName(category.getCategoryName())
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

    // Master Data CRUD methods
    public CategoryResponse createCategoryMasterData(CreateCategoryRequest request) {
        String categoryType = normalizeCategoryType(request.getCategoryType());
        Category category = Category.builder()
                .categoryName(request.getCategoryName())
            .categoryType(categoryType)
                .build();

        Category savedCategory = categoryRepository.save(category);

        return CategoryResponse.builder()
                .categoryId(savedCategory.getCategoryId())
                .categoryName(savedCategory.getCategoryName())
                .categoryType(savedCategory.getCategoryType())
                .build();
    }

    public CategoryResponse updateCategoryMasterData(Integer id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (request.getCategoryName() != null) {
            category.setCategoryName(request.getCategoryName());
        }
        if (request.getCategoryType() != null) {
            category.setCategoryType(normalizeCategoryType(request.getCategoryType()));
        }

        Category savedCategory = categoryRepository.save(category);

        return CategoryResponse.builder()
                .categoryId(savedCategory.getCategoryId())
                .categoryName(savedCategory.getCategoryName())
                .categoryType(savedCategory.getCategoryType())
                .build();
    }

    public CategoryResponse deleteCategoryMasterData(Integer id, java.util.Map<String, Object> body) {
        Category category = categoryRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (softDeleteEntity(category)) {
            categoryRepository.save(category);
        } else {
            categoryRepository.delete(category);
        }

        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .categoryType(category.getCategoryType())
                .build();
    }

    public List<SubCategoryResponse> getAllSubCategories() {
        List<SubCategory> subCategories = subCategoryRepository.findAllWithCategory();
        return subCategories.stream()
                .map(this::toSubCategoryResponse)
                .collect(Collectors.toList());
    }

    public SubCategoryResponse createSubCategoryMasterData(Map<String, Object> request) {
        String subCategoryName = requireString(request, "subCategoryName", "Subcategory name is required");
        Long categoryId = requireLong(request, "categoryId", "Category is required");

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        SubCategory savedSubCategory = subCategoryRepository.save(SubCategory.builder()
                .subCategoryName(subCategoryName)
                .category(category)
                .build());

        return toSubCategoryResponse(savedSubCategory);
    }

    public SubCategoryResponse updateSubCategoryMasterData(Long id, Map<String, Object> request) {
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found"));

        if (request.containsKey("subCategoryName")) {
            String subCategoryName = stringValue(request.get("subCategoryName"));
            if (subCategoryName != null && !subCategoryName.trim().isEmpty()) {
                subCategory.setSubCategoryName(subCategoryName.trim());
            }
        }

        if (request.containsKey("categoryId")) {
            Long categoryId = longValue(request.get("categoryId"));
            if (categoryId == null) {
                throw new BadRequestExceptions("Category is required");
            }
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            subCategory.setCategory(category);
        }

        SubCategory savedSubCategory = subCategoryRepository.save(subCategory);
        return toSubCategoryResponse(savedSubCategory);
    }

    public SubCategoryResponse deleteSubCategoryMasterData(Long id, Map<String, Object> body) {
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found"));

        subCategoryRepository.delete(subCategory);
        return toSubCategoryResponse(subCategory);
    }

    private SubCategoryResponse toSubCategoryResponse(SubCategory subCategory) {
        return SubCategoryResponse.builder()
                .subCategoryId(subCategory.getSubCategoryId())
                .subCategoryName(subCategory.getSubCategoryName())
                .categoryId(subCategory.getCategory() != null ? subCategory.getCategory().getCategoryId() : null)
                .categoryName(subCategory.getCategory() != null ? subCategory.getCategory().getCategoryName() : null)
                .build();
    }

    private String normalizeCategoryType(String categoryType) {
        String normalized = categoryType == null ? "" : categoryType.trim().toLowerCase();

        if (normalized.isEmpty()) {
            return TypeConstants.COMPANIES;
        }

        if (!TypeConstants.COMPANIES.equals(normalized) && !TypeConstants.JOBS.equals(normalized)) {
            throw new BadRequestExceptions(MessageConstants.Validation.INVALID_TYPE_COMPANIES_OR_JOBS);
        }

        return normalized;
    }

    private String requireString(Map<String, Object> request, String key, String message) {
        String value = stringValue(request != null ? request.get(key) : null);
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestExceptions(message);
        }
        return value.trim();
    }

    private Long requireLong(Map<String, Object> request, String key, String message) {
        Long value = longValue(request != null ? request.get(key) : null);
        if (value == null) {
            throw new BadRequestExceptions(message);
        }
        return value;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean softDeleteEntity(Category category) {
        try {
            category.getClass().getMethod("setIsDeleted", Boolean.class).invoke(category, true);
            return true;
        } catch (NoSuchMethodException ignored) {
            try {
                category.getClass().getMethod("setIsDeleted", boolean.class).invoke(category, true);
                return true;
            } catch (Exception ignoredAgain) {
                return false;
            }
        } catch (Exception ignored) {
            return false;
        }
    }


}
