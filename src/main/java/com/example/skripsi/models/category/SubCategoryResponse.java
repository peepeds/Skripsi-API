package com.example.skripsi.models.category;

import com.example.skripsi.models.job.JobListItemResponse;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SubCategoryResponse {
    private Long subCategoryId;
    private String subCategoryName;
    private Long categoryId;
    private String categoryName;
    private List<JobListItemResponse> jobs;
}

