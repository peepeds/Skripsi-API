package com.example.skripsi.controllers;

import com.example.skripsi.interfaces.IReviewService;
import com.example.skripsi.models.WebResponse;
import com.example.skripsi.models.review.CreateReviewRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("review")
public class ReviewController {

    private final IReviewService reviewService;

    public ReviewController(IReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/jobOptions")
    public WebResponse<?> searchJobOptions(@RequestParam(value = "query", required = false) String query) {
        var result = reviewService.searchJobOptions(query);
        return WebResponse.builder()
                .success(true)
                .message("Successfully search job options")
                .result(result)
                .build();
    }

    @GetMapping("/recent")
    public WebResponse<?> getRecentReviews() {
        var result = reviewService.getRecentReviews();
        return WebResponse.builder()
                .success(true)
                .message("Successfully retrieved recent reviews")
                .result(result)
                .build();
    }

    @PostMapping("/{slug}")
    @PreAuthorize("isAuthenticated()")
    public WebResponse<?> submitReview(@PathVariable String slug, @Valid @RequestBody CreateReviewRequest request) {
        var result = reviewService.submitReview(slug, request);
        return WebResponse.builder()
                .success(true)
                .message("Review submitted successfully")
                .result(result)
                .build();
    }

    @GetMapping("/{slug}/summary")
    public WebResponse<?> getCompanySummary(@PathVariable String slug) {
        var result = reviewService.getCompanySummary(slug);
        return WebResponse.builder()
                .success(true)
                .message("Successfully retrieved company review summary")
                .result(result)
                .build();
    }

    @GetMapping("/{slug}")
    public WebResponse<?> getCompanyReviews(
            @PathVariable String slug,
            @RequestParam(value = "order", defaultValue = "popular") String order,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        var result = reviewService.getCompanyReviews(slug, order, cursor, limit);
        return WebResponse.builder()
                .success(true)
                .message("Successfully retrieved company reviews")
                .result(result)
                .build();
    }
}
