package com.example.skripsi.models.category;

import com.example.skripsi.models.job.JobListItemResponse;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CategoryResponse {
    private Long categoryId;
    private String categoryName;
    private String categoryType;
    private List<SubCategoryResponse> subCategories;
    private List<JobListItemResponse> jobs;
}

