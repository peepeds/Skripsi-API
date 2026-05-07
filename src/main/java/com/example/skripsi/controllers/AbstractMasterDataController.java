package com.example.skripsi.controllers;

import com.example.skripsi.models.WebResponse;
import com.example.skripsi.services.AbstractMasterDataService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

public abstract class AbstractMasterDataController<Service extends AbstractMasterDataService<?, Response, CreateRequest, UpdateRequest>, Response, CreateRequest, UpdateRequest> {
    
    protected final Service service;
    
    protected AbstractMasterDataController(Service service) {
        this.service = service;
    }

    @GetMapping("")
    protected WebResponse<?> getAll() {
        var results = service.getAll();
        return WebResponse.builder()
                .success(true)
                .message(getGetAllMessage())
                .result(results)
                .build();
    }

    @PostMapping("")
    @PreAuthorize("hasAnyRole('ADMIN')")
    protected WebResponse<?> create(@Valid @RequestBody CreateRequest request) {
        var result = service.create(request);
        return WebResponse.builder()
                .success(true)
                .message(getCreateMessage())
                .result(result)
                .build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    protected WebResponse<?> update(@Valid @RequestBody UpdateRequest request, @PathVariable Integer id) {
        var result = service.update(id, request);
        return WebResponse.builder()
                .success(true)
                .message(getUpdateMessage())
                .result(result)
                .build();
    }

    protected abstract String getGetAllMessage();

    protected abstract String getCreateMessage();

    protected abstract String getUpdateMessage();
}
