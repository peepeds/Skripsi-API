package com.example.skripsi.repositories;

import com.example.skripsi.entities.Region;
import com.example.skripsi.models.region.RegionOptionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RegionRepository extends JpaRepository<Region,Integer> {
    public List<Region> findAll();

    @Query("SELECT new com.example.skripsi.models.region.RegionOptionResponse(r.regionId, r.regionName) " +
            "FROM Region r")
    public List<RegionOptionResponse> findAllRegionOptions();
    boolean existsByRegionId(Integer regionId);
    boolean existsByRegionNameIgnoreCase(String regionName);
}
