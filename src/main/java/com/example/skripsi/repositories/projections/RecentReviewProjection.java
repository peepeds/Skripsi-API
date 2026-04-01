package com.example.skripsi.repositories.projections;

import java.time.Instant;

public interface RecentReviewProjection {
    String getTestimony();
    String getCreatedByName();
    Double getAverageRating();
    String getCompanyName();
    String getCompanyCategory();
    String getCompanyWebsite();
    String getJobTitle();
    Instant getCreatedAt();
}
