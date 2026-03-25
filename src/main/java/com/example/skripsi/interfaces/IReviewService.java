package com.example.skripsi.interfaces;

import com.example.skripsi.models.review.CreateReviewRequest;
import com.example.skripsi.models.review.ReviewResponse;
import com.example.skripsi.models.job.JobListItemResponse;

import java.util.List;

public interface IReviewService {
    ReviewResponse submitReview(Long companyId, CreateReviewRequest request, Long userId);
    ReviewResponse submitReview(String slug, CreateReviewRequest request);
    List<JobListItemResponse> searchJobOptions(String query);
}
