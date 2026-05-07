package com.example.skripsi.interfaces;

import com.example.skripsi.models.dashboard.DashboardStatisticsResponse;
import com.example.skripsi.models.dashboard.TopReviewCompanyResponse;
import com.example.skripsi.models.dashboard.TrendMonthResponse;

import java.util.List;

public interface IDashboardService {
    DashboardStatisticsResponse getStatistics();
    List<TopReviewCompanyResponse> getTopReviews();
    List<TrendMonthResponse> getTrends();
}
