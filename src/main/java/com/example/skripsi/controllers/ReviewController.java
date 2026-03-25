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
}
