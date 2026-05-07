package com.example.skripsi.controllers;

import com.example.skripsi.entities.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.*;
import com.example.skripsi.models.company.*;
import com.example.skripsi.securities.SecurityUtils;
import com.example.skripsi.services.CompanyService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
@RequestMapping("company")
public class CompanyController {

    private final ICompanyService companyService;
    private final CompanyService companyServiceImpl;
    private final SecurityUtils securityUtils;

    public CompanyController(ICompanyService companyService, CompanyService companyServiceImpl, SecurityUtils securityUtils) {
        this.companyService = companyService;
        this.companyServiceImpl = companyServiceImpl;
        this.securityUtils = securityUtils;
    }

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> xgetCompanies(@RequestParam(value = "cursor", required = false) Long cursor,
                                       @RequestParam(value = "limit", required = false) Integer limit,
                                       @RequestParam(value = "search", required = false) String search,
                                       @RequestParam(value = "sort", required = false) String sort) {
        if (search != null && !search.isEmpty()) {
            var results = companyService.searchCompanies(search);
            return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", results));
        } else {
            int limitVal = limit != null ? limit : 15;
            var results = companyService.getCompany(cursor, limitVal, sort);
            return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", results));
        }
    }

    @PostMapping("")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        var result = companyServiceImpl.createCompanyMasterData(request);
        return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", result));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateCompany(@PathVariable Integer id, @Valid @RequestBody UpdateCompanyRequest request) {
        var result = companyServiceImpl.updateCompanyMasterData(id, request);
        return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteCompany(@PathVariable Integer id, @RequestBody(required = false) java.util.Map<String, Object> body) {
        var result = companyServiceImpl.deleteCompanyMasterData(id, body);
        return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", result));
    }

    @PostMapping("/requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> submitCompanyRequest(@Valid @RequestBody CreateCompanyRequestRequest request) {
        var result = companyService.submitCompanyRequest(request);
        return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", result));
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCompanyRequests(@RequestParam(value = "status", required = false) CompanyRequestStatus status,
                                             @RequestParam(value = "cursor", required = false) Long cursor,
                                             @RequestParam(value = "limit", defaultValue = "15") int limit) {
        var results = companyService.getCompanyRequests(status, cursor, limit);
        return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", results));
    }

    @PatchMapping("/requests/{requestId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> reviewCompanyRequest(@PathVariable Long requestId,
                                               @Valid @RequestBody ReviewCompanyRequestRequest request) {
        var result = companyService.reviewCompanyRequest(requestId, request);
        return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", result));
    }

    @GetMapping("/request/{requestId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getCompanyRequestDetail(@PathVariable Long requestId) {
        var result = companyService.getCompanyRequestDetail(requestId);
        return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", result));
    }

    @GetMapping("/top-ratings")
    public ResponseEntity<Map<String, Object>> getTopCompaniesByAverage() {
        Long userId = securityUtils.getOptionalCurrentUserId().orElse(null);
        var result = companyService.getTopCompaniesAvgRating(userId);
        return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", result));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Map<String, Object>> getCompanyBySlug(@PathVariable String slug) {
        var result = companyService.getCompanyBySlug(slug);
        return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", result));
    }

    @PostMapping("/{slug}/save")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> saveCompany(@PathVariable String slug,
                                      @Valid @RequestBody SaveCompanyRequest request) {
        var result = companyService.saveCompany(slug, request);
        return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", result));
    }
}
