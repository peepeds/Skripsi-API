package com.example.skripsi.interfaces;

import com.example.skripsi.models.review.CreateReviewRequest;
import com.example.skripsi.models.review.ReviewResponse;
import com.example.skripsi.models.review.ReviewSummaryResponse;
import com.example.skripsi.models.review.CompanyReviewsResponse;
import com.example.skripsi.models.review.RecentReviewResponse;
import com.example.skripsi.models.job.JobListItemResponse;
import com.example.skripsi.models.CursorPageResponse;

import java.util.List;
import java.util.Map;

public interface IReviewService {
    ReviewResponse submitReview(Long companyId, CreateReviewRequest request, Long userId);
    ReviewResponse submitReview(String slug, CreateReviewRequest request);
    List<JobListItemResponse> searchJobOptions(String query);
    ReviewSummaryResponse getCompanySummary(String slug);
    CursorPageResponse<CompanyReviewsResponse.ReviewItem> getCompanyReviews(String slug, String order, Long cursor, int limit);
    RecentReviewResponse getRecentReviews();

    Map<Long, Double> getRatingsByCompanyIds(List<Long> companyIds);

    Map<Long, Long> getReviewCountsByCompanyIds(List<Long> companyIds);

    List<Long> getTop10CompanyIdsByRating();
}
