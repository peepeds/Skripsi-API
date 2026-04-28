package com.example.skripsi.controllers;

import com.example.skripsi.entities.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.*;
import com.example.skripsi.models.company.*;
import com.example.skripsi.securities.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("company")
public class CompanyController {

    private final ICompanyService companyService;
    private final SecurityUtils securityUtils;

    public CompanyController(ICompanyService companyService, SecurityUtils securityUtils) {
        this.companyService = companyService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("")
    public WebResponse<?> getCompanies(@RequestParam(value = "cursor", required = false) Long cursor,
                                       @RequestParam(value = "limit", required = false) Integer limit,
                                       @RequestParam(value = "search", required = false) String search,
                                       @RequestParam(value = "sort", required = false) String sort) {
        if (search != null && !search.isEmpty()) {
            var results = companyService.searchCompanies(search);
            return WebResponse.builder()
                    .success(true)
                    .message("Successfully search companies")
                    .result(results)
                    .build();
        } else {
            int limitVal = limit != null ? limit : 15;
            var results = companyService.getCompany(cursor, limitVal, sort);
            return WebResponse.builder()
                    .success(true)
                    .message("Successfully Get Companies data")
                    .meta(results.getMeta())
                    .result(results.getResult())
                    .build();
        }
    }

    @PostMapping("/requests")
    @PreAuthorize("isAuthenticated()")
    public WebResponse<?> submitCompanyRequest(@Valid @RequestBody CreateCompanyRequestRequest request) {
        var result = companyService.submitCompanyRequest(request);
        return WebResponse.builder()
                .success(true)
                .message("Successfully submit company request")
                .result(result)
                .build();
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public WebResponse<?> getCompanyRequests(@RequestParam(value = "status", required = false) CompanyRequestStatus status,
                                             @RequestParam(value = "cursor", required = false) Long cursor,
                                             @RequestParam(value = "limit", defaultValue = "15") int limit) {
        var results = companyService.getCompanyRequests(status, cursor, limit);
        return WebResponse.builder()
                .success(true)
                .message("Successfully get company requests")
                .meta(results.getMeta())
                .result(results.getResult())
                .build();
    }

    @PatchMapping("/requests/{requestId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public WebResponse<?> reviewCompanyRequest(@PathVariable Long requestId,
                                               @Valid @RequestBody ReviewCompanyRequestRequest request) {
        var result = companyService.reviewCompanyRequest(requestId, request);
        return WebResponse.builder()
                .success(true)
                .message("Successfully review company request")
                .result(result)
                .build();
    }

    @GetMapping("/request/{requestId}")
    @PreAuthorize("isAuthenticated()")
    public WebResponse<?> getCompanyRequestDetail(@PathVariable Long requestId) {
        var result = companyService.getCompanyRequestDetail(requestId);
        return WebResponse.builder()
                .success(true)
                .message("Company request detail")
                .result(result)
                .build();
    }

    @GetMapping("/top-ratings")
    public WebResponse<?> getTopCompaniesByAverage() {
        Long userId = securityUtils.getOptionalCurrentUserId().orElse(null);
        var result = companyService.getTopCompaniesAvgRating(userId);
        return WebResponse.builder()
                .success(true)
                .message("Successfully get top 10 companies by rating")
                .result(result)
                .build();
    }

    @GetMapping("/{slug}")
    public WebResponse<?> getCompanyBySlug(@PathVariable String slug) {
        var result = companyService.getCompanyBySlug(slug);
        return WebResponse.builder()
                .success(true)
                .message("Successfully get company profile")
                .result(result)
                .build();
    }

    @PostMapping("/{slug}/save")
    @PreAuthorize("isAuthenticated()")
    public WebResponse<?> saveCompany(@PathVariable String slug,
                                      @Valid @RequestBody SaveCompanyRequest request) {
        var result = companyService.saveCompany(slug, request);
        return WebResponse.builder()
                .success(true)
                .message("Successfully updated company save status")
                .result(result)
                .build();
    }
}
