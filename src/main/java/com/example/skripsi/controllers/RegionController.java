package com.example.skripsi.controllers;

import com.example.skripsi.interfaces.IRegionService;
import com.example.skripsi.models.WebResponse;
import com.example.skripsi.models.region.CreateRegionRequest;
import com.example.skripsi.models.region.UpdateRegionRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("region")
public class RegionController {
    private final IRegionService regionService;

    public RegionController(IRegionService regionService){
        this.regionService = regionService;
    }

    @GetMapping("")
    public WebResponse<?> getAllRegion(){
        var results = regionService.getAllRegion();
        return WebResponse.builder()
                .success(true)
                .message("Successfully Get All Regions Data")
                .result(results)
                .build();
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public WebResponse<?> createRegion(@Valid @RequestBody CreateRegionRequest request) {
        var result = regionService.createRegion(request);

        return WebResponse.builder()
                .success(true)
                .message("Successfully Created new Region")
                .result(result)
                .build();
    }

    @PatchMapping("/{regionId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public WebResponse<?> updateRegion(@Valid @RequestBody UpdateRegionRequest updateRegionRequest, @PathVariable Integer regionId) {
        var result = regionService.updateRegion(updateRegionRequest, regionId);

        return WebResponse.builder()
                        .success(true)
                        .message("Successfully Updated Region")
                        .result(result)
                        .build();

    }

    @GetMapping("/options")
    public WebResponse<?> getAllRegionOptions(){

        var results = regionService.getAllRegionOptions();
        return WebResponse.builder()
                .success(true)
                .message("Successfully Get Regions")
                .result(results)
                .build();
    }
}