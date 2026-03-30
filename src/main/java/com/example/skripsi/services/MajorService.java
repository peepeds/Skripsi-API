package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.major.*;
import com.example.skripsi.repositories.*;
import com.example.skripsi.securities.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MajorService extends AbstractMasterDataService<Major, MajorResponse, CreateMajorRequest, UpdateMajorRequest> implements IMajorService {

    private final MajorRepository majorRepository;
    private final RegionRepository regionRepository;
    private final DepartmentRepository departmentRepository;

    public MajorService(MajorRepository majorRepository,
                        DepartmentRepository departmentRepository,
                        RegionRepository regionRepository,
                        UserRepository userRepository,
                        SecurityUtils securityUtils) {
        super(majorRepository, userRepository, securityUtils);
        this.majorRepository = majorRepository;
        this.departmentRepository = departmentRepository;
        this.regionRepository = regionRepository;
    }

    public List<MajorResponse> getAllMajor() {
        return getAll();
    }

    @Override
    public List<MajorOptionResponse> getAllMajorOptions() {
        return majorRepository.findAllOptions();
    }

    public MajorResponse createMajor(CreateMajorRequest request) {
        return create(request);
    }

    public MajorResponse updateMajor(Integer majorId, UpdateMajorRequest request) {
        return update(majorId, request);
    }

    @Override
    protected void validateBeforeCreate(CreateMajorRequest request) {
        boolean isExists = majorRepository.existsByMajorNameIgnoreCase(request.getMajorName().trim());
        if (isExists) {
            throw new BadRequestExceptions("Major Name already exists!");
        }
        
        regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new ResourceNotFoundException("Region not found"));
        
        departmentRepository.findById(request.getDeptId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    }

    @Override
    protected Major buildEntity(CreateMajorRequest request, Long userId) {
        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new ResourceNotFoundException("Region not found"));
        
        Department department = departmentRepository.findById(request.getDeptId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        return Major.builder()
                .majorName(request.getMajorName())
                .region(region)
                .department(department)
                .createdBy(userId)
                .createdAt(OffsetDateTime.now())
                .active(true)
                .build();
    }

    @Override
    protected void updateEntityFields(Major entity, UpdateMajorRequest request) {
        if (request.getMajorName() != null) {
            entity.setMajorName(request.getMajorName());
        }
        
        if (request.getRegionId() != null) {
            Region region = regionRepository.findById(request.getRegionId())
                    .orElseThrow(() -> new BadRequestExceptions("Region not found"));
            entity.setRegion(region);
        }
        
        if (request.getDeptId() != null) {
            Department department = departmentRepository.findById(request.getDeptId())
                    .orElseThrow(() -> new BadRequestExceptions("Department not found"));
            entity.setDepartment(department);
        }
        
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }
    }

    @Override
    protected Major setUpdateAuditFields(Major entity, Long userId) {
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(OffsetDateTime.now());
        return entity;
    }

    @Override
    protected MajorResponse toResponse(Major major, Map<Long, User> userMap) {
        String createdByUser = resolveUsername(major.getCreatedBy(), userMap);
        String updatedByUser = resolveUsername(major.getUpdatedBy(), userMap);

        return MajorResponse.builder()
                .majorId(major.getMajorId())
                .majorName(major.getMajorName())
                .deptName(major.getDepartment().getDeptName())
                .regionName(major.getRegion().getRegionName())
                .createdAt(major.getCreatedAt())
                .createdBy(createdByUser)
                .updatedAt(major.getUpdatedAt())
                .updatedBy(updatedByUser)
                .active(major.getActive())
                .build();
    }

    @Override
    protected String getNotFoundErrorMessage() {
        return "Major not found!";
    }
}
