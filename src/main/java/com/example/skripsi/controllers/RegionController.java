package com.example.skripsi.controllers;

import com.example.skripsi.models.*;
import com.example.skripsi.models.region.*;
import com.example.skripsi.services.RegionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("region")
public class RegionController extends AbstractMasterDataController<RegionService, RegionResponse, CreateRegionRequest, UpdateRegionRequest> {

    public RegionController(RegionService regionService) {
        super(regionService);
    }

    @GetMapping("/options")
    public WebResponse<?> getAllRegionOptions() {
        var results = service.getAllRegionOptions();
        return WebResponse.builder()
                .success(true)
                .message("Successfully Get Regions")
                .result(results)
                .build();
    }

    @Override
    protected String getGetAllMessage() {
        return "Successfully Get All Regions Data";
    }

    @Override
    protected String getCreateMessage() {
        return "Successfully Created new Region";
    }

    @Override
    protected String getUpdateMessage() {
        return "Successfully Updated Region";
    }
}