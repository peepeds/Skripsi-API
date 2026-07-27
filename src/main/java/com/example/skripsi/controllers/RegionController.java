package com.example.skripsi.controllers;

import com.example.skripsi.interfaces.IRegionService;
import com.example.skripsi.models.*;
import com.example.skripsi.models.region.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("region")
public class RegionController extends AbstractMasterDataOptionsController<IRegionService, RegionResponse, CreateRegionRequest, UpdateRegionRequest> {

    public RegionController(IRegionService regionService) {
        super(regionService, regionService::getAllRegionOptions, "Successfully Get Regions");
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
