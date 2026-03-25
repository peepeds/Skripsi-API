package com.example.skripsi.controllers;

import com.example.skripsi.models.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/ping")
    //@PreAuthorize("hasAnyRole('ADMIN', 'EPC')")
    public WebResponse<?> ping(){
        return WebResponse.builder()
                .success(true)
                .message("pong")
                .result("ping pong")
                .build();
    }
}
