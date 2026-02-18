package com.example.skripsi.controllers;

import com.example.skripsi.models.WebResponse;
import com.example.skripsi.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public WebResponse<?> getAllUserByUserPrivilege(){
        var userResponses = userService.getAllUserByUserPrivilege();

        return WebResponse.builder()
                .success(true)
                .message("Successfully Get All User")
                .result(userResponses)
                .build();
    }

    @GetMapping("/me")
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


}
