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
    public WebResponse<?> getAllUserByUserPrivilege(){
        var userResponses = userService.getAllUserByUserPrivilege();

        return WebResponse.builder()
                .success(true)
                .message("Successfully Get All User")
                .result(userResponses)
                .build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public WebResponse<?> checkIdentity() {

        var userProfile = userService.getUserProfile();

        return WebResponse.builder()
                .success(true)
                .message("Successfully get profile")
                .result(userProfile)
                .build();
    }


    @PostMapping("/check-email")
    public ResponseEntity<WebResponse<Object>> checkEmail(
            @RequestBody Map<String, String> body
    ) {
        String email = body.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(
                    WebResponse.builder()
                            .success(false)
                            .message("Email is required")
                            .build()
            );
        }

        boolean exists = userService.emailExists(email.toLowerCase());

        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    WebResponse.builder()
                            .success(false)
                            .message("Email already used!")
                            .build()
            );
        }

        return ResponseEntity.ok(
                WebResponse.builder()
                        .success(true)
                        .message("Email is available")
                        .build()
        );
    }

    @PostMapping("/certificate")
    @PreAuthorize("isAuthenticated()")
    public WebResponse<?> uploadCertificate(@RequestBody CreateCertificateRequest request) {
        CertificateResponse response = userService.submitCertificateRequest(request);

        return WebResponse.builder()
                .success(true)
                .message("Certificate request submitted successfully")
                .result(response)
                .build();
    }

    @PatchMapping("/certificate/requests/{requestId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public WebResponse<?> reviewCertificate(@PathVariable Long requestId, @RequestBody ReviewCertificateRequest request) {
        CertificateResponse response = userService.reviewCertificateRequest(requestId, request);

        return WebResponse.builder()
                .success(true)
                .message("Certificate request reviewed successfully")
                .result(response)
                .build();
    }

    @GetMapping("/certificate/request/{requestId}")
    @PreAuthorize("isAuthenticated()")
    public WebResponse<?> getCertificateRequestDetail(@PathVariable Long requestId) {
        CertificateRequestDetailResponse result = userService.getCertificateRequestDetail(requestId);

        return WebResponse.builder()
                .success(true)
                .message("Certificate request detail")
                .result(result)
                .build();
    }

    @GetMapping("/certificate/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public WebResponse<?> getCertificateRequests(@RequestParam(value = "status", required = false) String status,
                                                 @RequestParam(value = "cursor", required = false) Long cursor,
                                                 @RequestParam(value = "limit", defaultValue = "15") int limit) {
        var results = userService.getCertificateRequests(status, cursor, limit);
        return WebResponse.builder()
                .success(true)
                .message("Successfully get certificate requests")
                .meta(results.getMeta())
                .result(results.getResult())
                .build();
    }

    @GetMapping("/my-bookmarks")
    @PreAuthorize("isAuthenticated()")
    public WebResponse<?> getMyBookmarks(
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        var results = companyService.getMyBookmarks(cursor, limit);

        return WebResponse.builder()
                .success(true)
                .message("Successfully get bookmarks")
                .meta(results.getMeta())
                .result(results.getResult())
                .build();
    }

    @GetMapping("/my-reviews")
    @PreAuthorize("isAuthenticated()")
    public WebResponse<?> getMyReviews(
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        var results = reviewService.getMyReviews(cursor, limit);

        return WebResponse.builder()
                .success(true)
                .message("Successfully get my reviews")
                .meta(results.getMeta())
                .result(results.getResult())
                .build();
    }

    @GetMapping("/my-certificates")
    @PreAuthorize("isAuthenticated()")
    public WebResponse<?> getMyCertificates() {
        var certificates = userService.getMyCertificates();

        return WebResponse.builder()
                .success(true)
                .message("Successfully get my certificates")
                .result(certificates)
                .build();
    }

}
