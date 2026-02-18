package com.example.skripsi.services;

import com.example.skripsi.entities.Department;
import com.example.skripsi.entities.Major;
import com.example.skripsi.entities.Region;
import com.example.skripsi.entities.User;
import com.example.skripsi.exceptions.BadRequestExceptions;
import com.example.skripsi.interfaces.IMajorService;
import com.example.skripsi.models.major.CreateMajorRequest;
import com.example.skripsi.models.major.MajorResponse;
import com.example.skripsi.models.major.MajorOptionResponse;
import com.example.skripsi.models.major.UpdateMajorRequest;
import com.example.skripsi.repositories.DepartmentRepository;
import com.example.skripsi.repositories.MajorRepository;
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
public class MajorService implements IMajorService {

    private final SecurityUtils securityUtils;
    private final MajorRepository majorRepository;
    private final RegionRepository regionRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public MajorService(MajorRepository majorRepository,
                        DepartmentRepository departmentRepository,
                        RegionRepository regionRepository,
                        UserRepository userRepository,
                        SecurityUtils securityUtils){
        this.majorRepository = majorRepository;
        this.departmentRepository = departmentRepository;
        this.regionRepository = regionRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    public List<MajorResponse> getAllMajor() {
        return majorRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MajorOptionResponse> getAllMajorOptions() {
        return majorRepository.findAllOptions();
    }

    public MajorResponse createMajor(CreateMajorRequest createMajorRequest) {
        Long userId = securityUtils.getCurrentUserId();
        boolean isExists = majorRepository.existsByMajorNameIgnoreCase(createMajorRequest.getMajorName().trim());

        if(isExists){
            throw new BadRequestExceptions("Major Name already exists!");
        }

        Region region = regionRepository.findById(createMajorRequest.getRegionId())
                .orElseThrow(() -> new RuntimeException("Region not found"));

        Department department = departmentRepository.findById(createMajorRequest.getDeptId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        Major major = Major.builder()
                .majorName(createMajorRequest.getMajorName())
                .region(region)
                .department(department)
                .createdBy(userId)
                .createdAt(OffsetDateTime.now())
                .active(true)
                .build();

        Major savedMajor = majorRepository.save(major);

        return toResponse(savedMajor);
    }

    @Async("taskExecutor")
    @Override
    public MajorResponse updateMajor(Integer majorId, UpdateMajorRequest updateMajorRequest) {
        Long userId = securityUtils.getCurrentUserId();
        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new BadRequestExceptions("Major not found"));

        if (updateMajorRequest.getMajorName() != null) {
            major.setMajorName(updateMajorRequest.getMajorName());
        }

        if (updateMajorRequest.getRegionId() != null) {
            Region region = regionRepository.findById(updateMajorRequest.getRegionId())
                    .orElseThrow(() -> new BadRequestExceptions("Region not found"));
            major.setRegion(region);
        }

        if (updateMajorRequest.getDeptId() != null) {
            Department department = departmentRepository.findById(updateMajorRequest.getDeptId())
                    .orElseThrow(() -> new BadRequestExceptions("Department not found"));
            major.setDepartment(department);
        }

        if (updateMajorRequest.getActive() != null) {
            major.setActive(updateMajorRequest.getActive());
        }

        major.setUpdatedBy(userId);
        major.setUpdatedAt(OffsetDateTime.now());

        Major savedMajor = majorRepository.save(major);

        return toResponse(savedMajor);
    }

    private MajorResponse toResponse(Major major){

        String createdByUser = userRepository.findByUserId(major.getCreatedBy())
                .map(User::getFirstName)
                .orElse(null);

        String updatedByUser = userRepository.findByUserId(major.getUpdatedBy())
                .map(User::getFirstName)
                .orElse(null);

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
}
