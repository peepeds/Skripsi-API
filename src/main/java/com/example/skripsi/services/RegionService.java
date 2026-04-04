package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.region.*;
import com.example.skripsi.repositories.*;
import com.example.skripsi.securities.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;


@Service
@Transactional
public class RegionService extends AbstractMasterDataService<Region, RegionResponse, CreateRegionRequest, UpdateRegionRequest> implements IRegionService {

    private final RegionRepository regionRepository;

    public RegionService(RegionRepository regionRepository,
                         IUserService userService,
                         SecurityUtils securityUtils) {
        super(regionRepository, userService, securityUtils);
        this.regionRepository = regionRepository;
    }

    public List<RegionResponse> getAllRegion() {
        return getAll();
    }

    public List<RegionOptionResponse> getAllRegionOptions() {
        return regionRepository.findAllRegionOptions();
    }

    public RegionResponse createRegion(CreateRegionRequest request) {
        return create(request);
    }

    public RegionResponse updateRegion(UpdateRegionRequest request, Integer regionId) {
        return update(regionId, request);
    }

    @Override
    protected void validateBeforeCreate(CreateRegionRequest request) {
        boolean isExists = regionRepository.existsByRegionNameIgnoreCase(request.getRegionName().trim());

        if (isExists) {
            throw new BadRequestExceptions("Region Name already exists!");
        }
    }

    @Override
    protected Region buildEntity(CreateRegionRequest request, Long userId) {
        return Region.builder()
                .regionName(request.getRegionName())
                .createdAt(OffsetDateTime.now())
                .createdBy(userId)
                .active(true)
                .build();
    }

    @Override
    protected void updateEntityFields(Region entity, UpdateRegionRequest request) {
        if (request.getRegionName() != null) {
            entity.setRegionName(request.getRegionName());
        }

        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }
    }

    @Override
    protected Region setUpdateAuditFields(Region entity, Long userId) {
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(OffsetDateTime.now());
        return entity;
    }

    @Override
    protected RegionResponse toResponse(Region region, Map<Long, String> userNameMap) {
        String createdByUser = resolveUsername(region.getCreatedBy(), userNameMap);
        String updatedByUser = resolveUsername(region.getUpdatedBy(), userNameMap);

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

    @Override
    protected String getNotFoundErrorMessage() {
        return "Region not found!";
    }

    @Override
    public Region findRegionById(Long id) {
        return regionRepository.findById(id.intValue())
                .orElseThrow(() -> new BadRequestExceptions("Region not found!"));
    }
}
