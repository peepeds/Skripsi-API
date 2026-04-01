package com.example.skripsi.controllers;

import com.example.skripsi.entities.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.*;
import com.example.skripsi.models.company.*;
import com.example.skripsi.models.constant.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("company")
public class CompanyController {

    private final ICompanyService companyService;

    public CompanyController(ICompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("")
    public WebResponse<?> getCompanies(@RequestParam(value = "cursor", required = false) Long cursor,
                                       @RequestParam(value = "limit", required = false) Integer limit,
                                       @RequestParam(value = "search", required = false) String search) {
        if (search != null && !search.isEmpty()) {
            var results = companyService.searchCompanies(search);
            return WebResponse.builder()
                    .success(true)
                    .message(MessageConstants.Success.SUCCESSFULLY_SEARCH_COMPANIES)
                    .result(results)
                    .build();
        } else {
            int limitVal = limit != null ? limit : 15;
            var results = companyService.getCompany(cursor, limitVal);
            return WebResponse.builder()
                    .success(true)
                    .message(MessageConstants.Success.SUCCESSFULLY_GET_COMPANIES)
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
                .message(MessageConstants.Success.SUCCESSFULLY_SUBMIT_COMPANY_REQUEST)
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
                .message(MessageConstants.Success.SUCCESSFULLY_GET_COMPANY_REQUESTS)
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
                .message(MessageConstants.Success.SUCCESSFULLY_REVIEW_COMPANY_REQUEST)
                .result(result)
                .build();
    }

    @GetMapping("/request/{requestId}")
    @PreAuthorize("isAuthenticated()")
    public WebResponse<?> getCompanyRequestDetail(@PathVariable Long requestId) {
        var result = companyService.getCompanyRequestDetail(requestId);
        return WebResponse.builder()
                .success(true)
                .message(MessageConstants.Success.COMPANY_REQUEST_DETAIL)
                .result(result)
                .build();
    }

    @GetMapping("/top-ratings")
    public WebResponse<?> getTopCompaniesByAverage() {
        var result = companyService.getTopCompaniesAvgRating();
        return WebResponse.builder()
                .success(true)
                .message(MessageConstants.Success.SUCCESSFULLY_GET_TOP_10_COMPANIES)
                .result(result)
                .build();
    }

    @GetMapping("/{slug}")
    public WebResponse<?> getCompanyBySlug(@PathVariable String slug) {
        var result = companyService.getCompanyBySlug(slug);
        return WebResponse.builder()
                .success(true)
                .message(MessageConstants.Success.SUCCESSFULLY_GET_COMPANY_PROFILE)
                .result(result)
                .build();
    }
}
