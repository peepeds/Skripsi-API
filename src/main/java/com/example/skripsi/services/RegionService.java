package com.example.skripsi.services;

import com.example.skripsi.entities.Region;
import com.example.skripsi.entities.User;
import com.example.skripsi.exceptions.BadRequestExceptions;
import com.example.skripsi.interfaces.IRegionService;
import com.example.skripsi.models.region.CreateRegionRequest;
import com.example.skripsi.models.region.RegionOptionResponse;
import com.example.skripsi.models.region.RegionResponse;
import com.example.skripsi.models.region.UpdateRegionRequest;
import com.example.skripsi.repositories.RegionRepository;
import com.example.skripsi.repositories.UserRepository;
import com.example.skripsi.securities.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RegionService implements IRegionService {

    private final SecurityUtils securityUtils;
    private final RegionRepository regionRepository;
    private final UserRepository userRepository;

    public RegionService(RegionRepository regionRepository,
                         UserRepository userRepository,
                         SecurityUtils securityUtils){
        this.regionRepository = regionRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    @Override
    public List<RegionResponse> getAllRegion() {
        return regionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RegionOptionResponse> getAllRegionOptions() {
        return regionRepository.findAllRegionOptions();
    }

    @Override
    public RegionResponse createRegion(CreateRegionRequest createRegionRequest) {
        Long userId = securityUtils.getCurrentUserId();
        boolean isExists = regionRepository.existsByRegionNameIgnoreCase(createRegionRequest.getRegionName().trim());

        if(isExists){
            throw new BadRequestExceptions("Region Name already exists!");
        }

        Region region = Region.builder()
                .regionName(createRegionRequest.getRegionName())
                .createdAt(OffsetDateTime.now())
                .createdBy(userId)
                .active(true)
                .build();
        Region savedRegion = regionRepository.save(region);

        return toResponse(savedRegion);
    }

    @Async("taskExecutor")
    @Override
    public RegionResponse updateRegion(UpdateRegionRequest updateRegionRequest, Integer regionId) {
        Long userId = securityUtils.getCurrentUserId();
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new BadRequestExceptions("Major not found"));

        if (updateRegionRequest.getRegionName() != null) {
            region.setRegionName(updateRegionRequest.getRegionName());
        }

        if (updateRegionRequest.getActive() != null){
            region.setActive(updateRegionRequest.getActive());
        }

        region.setUpdatedBy(userId);
        region.setUpdatedAt(OffsetDateTime.now());

        Region savedRegion = regionRepository.save(region);

        return toResponse(savedRegion);
    }

    private RegionResponse toResponse(Region region){
        String createdByUser = userRepository.findByUserId(region.getCreatedBy())
                .map(User::getFirstName)
                .orElse(null);

        String updatedByUser = userRepository.findByUserId(region.getUpdatedBy())
                .map(User::getFirstName)
                .orElse(null);

        return RegionResponse.builder()
                .regionId(region.getRegionId())
                .regionName(region.getRegionName())
                .createdAt(region.getCreatedAt())
                .createdBy(createdByUser)
                .updatedAt(region.getUpdatedAt())
                .updatedBy(updatedByUser)
                .active(region.getActive())
                .build();
    }
}
