package com.example.skripsi.controllers;

import com.example.skripsi.interfaces.ICompanyService;
import com.example.skripsi.interfaces.IReviewService;
import com.example.skripsi.models.*;
import com.example.skripsi.models.user.*;
import com.example.skripsi.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("user")
public class UserController {

    private final UserService userService;
    private final ICompanyService companyService;
    private final IReviewService reviewService;

    public UserController(UserService userService, ICompanyService companyService, IReviewService reviewService){
        this.userService = userService;
        this.companyService = companyService;
        this.reviewService = reviewService;
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Map<String, Object>> getAllUserByUserPrivilege(){
        var userResponses = userService.getAllUserByUserPrivilege();

                return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", userResponses));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Map<String, Object>> checkIdentity() {

        var userProfile = userService.getUserProfile();

                return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", userProfile));
    }


    @PostMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(
            @RequestBody Map<String, String> body
    ) {
        String email = body.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email is required", "result", Map.of()));
        }

        boolean exists = userService.emailExists(email.toLowerCase());

        if (exists) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "message", "Email already used!", "result", Map.of()));
        }

                return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", Map.of("email", email)));
    }

    @PostMapping("/certificate")
    @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Map<String, Object>> uploadCertificate(@RequestBody CreateCertificateRequest request) {
        CertificateResponse response = userService.submitCertificateRequest(request);

                return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", response));
    }

    @PatchMapping("/certificate/requests/{requestId}/review")
    @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Map<String, Object>> reviewCertificate(@PathVariable Long requestId, @RequestBody ReviewCertificateRequest request) {
        CertificateResponse response = userService.reviewCertificateRequest(requestId, request);

                return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", response));
    }

    @GetMapping("/certificate/request/{requestId}")
    @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Map<String, Object>> getCertificateRequestDetail(@PathVariable Long requestId) {
        CertificateRequestDetailResponse result = userService.getCertificateRequestDetail(requestId);

                return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", result));
    }

    @GetMapping("/certificate/requests")
    @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Map<String, Object>> getCertificateRequests(@RequestParam(value = "status", required = false) String status,
                                                 @RequestParam(value = "cursor", required = false) Long cursor,
                                                 @RequestParam(value = "limit", defaultValue = "15") int limit) {
        var results = userService.getCertificateRequests(status, cursor, limit);
                return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", results));
    }

    @GetMapping("/my-bookmarks")
    @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Map<String, Object>> getMyBookmarks(
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        var results = companyService.getMyBookmarks(cursor, limit);

                return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", results));
    }

    @GetMapping("/my-reviews")
    @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Map<String, Object>> getMyReviews(
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        var results = reviewService.getMyReviews(cursor, limit);

                return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", results));
    }

    @GetMapping("/my-certificates")
    @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Map<String, Object>> getMyCertificates() {
        var certificates = userService.getMyCertificates();

                return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", certificates));
    }

}
