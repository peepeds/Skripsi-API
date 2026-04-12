package com.example.skripsi.controllers;

import com.example.skripsi.interfaces.ICompanyService;
import com.example.skripsi.models.*;
import com.example.skripsi.models.user.*;
import com.example.skripsi.models.constant.*;
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

    public UserController(UserService userService, ICompanyService companyService){
        this.userService = userService;
        this.companyService = companyService;
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public WebResponse<?> getAllUserByUserPrivilege(){
        var userResponses = userService.getAllUserByUserPrivilege();

        return WebResponse.builder()
                .success(true)
                .message(MessageConstants.Success.SUCCESSFULLY_GET_ALL_USER)
                .result(userResponses)
                .build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public WebResponse<?> checkIdentity() {

        var userProfile = userService.getUserProfile();

        return WebResponse.builder()
                .success(true)
                .message(MessageConstants.Success.SUCCESSFULLY_GET_PROFILE)
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
                            .message(MessageConstants.Success.EMAIL_IS_REQUIRED)
                            .build()
            );
        }

        boolean exists = userService.emailExists(email.toLowerCase());

        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    WebResponse.builder()
                            .success(false)
                            .message(MessageConstants.Success.EMAIL_ALREADY_USED_CONFLICT)
                            .build()
            );
        }

        return ResponseEntity.ok(
                WebResponse.builder()
                        .success(true)
                        .message(MessageConstants.Success.EMAIL_IS_AVAILABLE)
                        .build()
        );
    }

    @PostMapping("/certificate")
    @PreAuthorize("isAuthenticated()")
    public WebResponse<?> uploadCertificate(@RequestBody CreateCertificateRequest request) {
        CertificateResponse response = userService.submitCertificateRequest(request);

        return WebResponse.builder()
                .success(true)
                .message(MessageConstants.Success.CERTIFICATE_REQUEST_SUBMITTED)
                .result(response)
                .build();
    }

    @PatchMapping("/certificate/requests/{requestId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public WebResponse<?> reviewCertificate(@PathVariable Long requestId, @RequestBody ReviewCertificateRequest request) {
        CertificateResponse response = userService.reviewCertificateRequest(requestId, request);

        return WebResponse.builder()
                .success(true)
                .message(MessageConstants.Success.CERTIFICATE_REQUEST_REVIEWED)
                .result(response)
                .build();
    }

    @GetMapping("/certificate/request/{requestId}")
    @PreAuthorize("isAuthenticated()")
    public WebResponse<?> getCertificateRequestDetail(@PathVariable Long requestId) {
        CertificateRequestDetailResponse result = userService.getCertificateRequestDetail(requestId);

        return WebResponse.builder()
                .success(true)
                .message(MessageConstants.Success.CERTIFICATE_REQUEST_DETAIL)
                .result(result)
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
                .message(MessageConstants.Success.SUCCESSFULLY_GET_BOOKMARKS)
                .meta(results.getMeta())
                .result(results.getResult())
                .build();
    }

}
