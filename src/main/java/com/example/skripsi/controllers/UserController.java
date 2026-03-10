package com.example.skripsi.controllers;

import com.example.skripsi.models.WebResponse;
import com.example.skripsi.models.user.CreateCertificateRequest;
import com.example.skripsi.models.user.CertificateResponse;
import com.example.skripsi.models.user.ReviewCertificateRequest;
import com.example.skripsi.models.user.CertificateRequestDetailResponse;
import com.example.skripsi.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
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


}
