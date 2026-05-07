package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.major.*;
import com.example.skripsi.repositories.MajorRepository;
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
    private final IRegionService regionService;
    private final IDepartmentService departmentService;

    public MajorService(MajorRepository majorRepository,
                        IDepartmentService departmentService,
                        IRegionService regionService,
                        IUserService userService,
                        SecurityUtils securityUtils) {
        super(majorRepository, userService, securityUtils);
        this.majorRepository = majorRepository;
        this.departmentService = departmentService;
        this.regionService = regionService;
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
        
        regionService.findRegionById(Long.valueOf(request.getRegionId()));
        departmentService.findDepartmentById(request.getDeptId());
    }

    @Override
    protected Major buildEntity(CreateMajorRequest request, Long userId) {
        Region region = regionService.findRegionById(Long.valueOf(request.getRegionId()));
        Department department = departmentService.findDepartmentById(request.getDeptId());

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
            Region region = regionService.findRegionById(Long.valueOf(request.getRegionId()));
            entity.setRegion(region);
        }
        
        if (request.getDeptId() != null) {
            Department department = departmentService.findDepartmentById(request.getDeptId());
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
    protected MajorResponse toResponse(Major major, Map<Long, String> userNameMap) {
        String createdByUser = resolveUsername(major.getCreatedBy(), userNameMap);
        String updatedByUser = resolveUsername(major.getUpdatedBy(), userNameMap);

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

    @Override
    public Major findMajorById(Long id) {
        return majorRepository.findById(id.intValue())
                .orElseThrow(() -> new BadRequestExceptions("Major not found!"));
    }
}
