package com.example.skripsi.controllers;

import com.example.skripsi.interfaces.IDashboardService;
import com.example.skripsi.models.WebResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("dashboard")
public class DashboardController {

    private final IDashboardService dashboardService;

    public DashboardController(IDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public WebResponse<?> getStatistics() {
        var result = dashboardService.getStatistics();
        return WebResponse.builder()
                .success(true)
                .message("Successfully get dashboard statistics")
                .result(result)
                .build();
    }

    @GetMapping("/top-reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public WebResponse<?> getTopReviews() {
        var result = dashboardService.getTopReviews();
        return WebResponse.builder()
                .success(true)
                .message("Successfully get top reviews companies")
                .result(result)
                .build();
    }

    @GetMapping("/trends")
    @PreAuthorize("hasRole('ADMIN')")
    public WebResponse<?> getTrends() {
        var result = dashboardService.getTrends();
        return WebResponse.builder()
                .success(true)
                .message("Successfully get dashboard trends")
                .result(result)
                .build();
    }
}
