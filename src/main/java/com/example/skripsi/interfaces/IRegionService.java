package com.example.skripsi.interfaces;

import com.example.skripsi.entities.Region;
import com.example.skripsi.models.region.*;

import java.util.List;

public interface IRegionService {
    List<RegionResponse> getAllRegion();
    List<RegionOptionResponse> getAllRegionOptions();
    RegionResponse createRegion(CreateRegionRequest createRegionRequest);
    RegionResponse updateRegion(UpdateRegionRequest updateRegionRequest, Integer regionId);
    Region findRegionById(Long id);
}
