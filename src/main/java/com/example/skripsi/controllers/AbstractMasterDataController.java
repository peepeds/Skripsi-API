package com.example.skripsi.controllers;

import com.example.skripsi.models.WebResponse;
import com.example.skripsi.services.AbstractMasterDataService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

public abstract class AbstractMasterDataController<Service extends AbstractMasterDataService<?, Response, CreateRequest, UpdateRequest>, Response, CreateRequest, UpdateRequest> {
    
    protected final Service service;
    
    protected AbstractMasterDataController(Service service) {
        this.service = service;
    }

    @GetMapping("")
    protected ResponseEntity<Map<String, Object>> getAll() {
        var results = service.getAll();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", getGetAllMessage(),
                "result", results
        ));
    }

    @PostMapping("")
    @PreAuthorize("hasAnyRole('ADMIN')")
    protected ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateRequest request) {
        var result = service.create(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", getCreateMessage(),
                "result", result
        ));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    protected ResponseEntity<Map<String, Object>> update(@Valid @RequestBody UpdateRequest request, @PathVariable Integer id) {
        var result = service.update(id, request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", getUpdateMessage(),
                "result", result
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    protected ResponseEntity<Map<String, Object>> delete(@PathVariable Integer id, @RequestBody(required = false) java.util.Map<String, Object> body) {
        var result = service.delete(id, body);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", getDeleteMessage(),
                "result", result
        ));
    }

    protected String getDeleteMessage() {
        return "Successfully Deleted";
    }

    protected abstract String getGetAllMessage();

    protected abstract String getCreateMessage();

    protected abstract String getUpdateMessage();
}
