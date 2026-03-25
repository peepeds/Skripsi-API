package com.example.skripsi.controllers;

import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("lookup")
public class LookupController {
    private final ILookupService lookupService;

    public LookupController(ILookupService lookupService) {
        this.lookupService = lookupService;
    }

    @GetMapping("")
    public WebResponse<?> getAllLookups() {
        var result = lookupService.getAllLookups();
        return WebResponse.builder()
                .success(true)
                .message("Successfully Get All Lookups")
                .result(result)
                .build();
    }

    @GetMapping("/{type}")
    public WebResponse<?> getLookupsByType(@PathVariable String type) {
        var result = lookupService.getLookupsByType(type);
        return WebResponse.builder()
                .success(true)
                .message("Successfully Get Lookups by Type")
                .result(result)
                .build();
    }
}
