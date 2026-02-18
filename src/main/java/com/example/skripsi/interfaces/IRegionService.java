package com.example.skripsi.interfaces;


import com.example.skripsi.models.major.UpdateMajorRequest;
import com.example.skripsi.models.region.CreateRegionRequest;
import com.example.skripsi.models.region.RegionOptionResponse;
import com.example.skripsi.models.region.RegionResponse;
import com.example.skripsi.models.region.UpdateRegionRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IRegionService {
    public List<RegionResponse> getAllRegion();
    public List<RegionOptionResponse> getAllRegionOptions();
    public RegionResponse createRegion(CreateRegionRequest createRegionRequest);
    public RegionResponse updateRegion(UpdateRegionRequest updateRegionRequest, Integer regionId);
}
