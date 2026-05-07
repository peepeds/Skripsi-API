package com.example.skripsi.repositories.projections;

public interface TopReviewCompanyProjection {
    Long getCompanyId();
    String getCompanyName();
    Double getAvgRating();
    Long getTotalReviews();
}
