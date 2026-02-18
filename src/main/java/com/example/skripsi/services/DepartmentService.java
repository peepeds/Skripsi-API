package com.example.skripsi.services;

import com.example.skripsi.entities.Department;
import com.example.skripsi.entities.User;
import com.example.skripsi.exceptions.BadRequestExceptions;
import com.example.skripsi.interfaces.IDepartmentService;
import com.example.skripsi.models.department.CreateDepartmentRequest;
import com.example.skripsi.models.department.DepartmentResponse;
import com.example.skripsi.models.department.UpdateDepartmentRequest;
import com.example.skripsi.repositories.DepartmentRepository;
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
public class DepartmentService implements IDepartmentService {

    private final SecurityUtils securityUtils;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DepartmentService(UserRepository userRepository,
                             DepartmentRepository departmentRepository,
                             SecurityUtils securityUtils){
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    @Override
    public List<DepartmentResponse> getAllDepartment() {
        return departmentRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentResponse createDepartment(CreateDepartmentRequest createDepartmentRequest) {
        Long userId = securityUtils.getCurrentUserId();
        boolean isExists = departmentRepository.existsByDeptNameIgnoreCase(createDepartmentRequest.getDeptName().trim());

        if (isExists){
            throw new BadRequestExceptions("Department Name already exists!");
        }

        Department dept = Department.builder()
                .deptName(createDepartmentRequest.getDeptName())
                .createdAt(OffsetDateTime.now())
                .createdBy(userId)
                .active(true)
                .build();

        Department savedDept = departmentRepository.save(dept);

        return toResponse(savedDept);
    }

    @Override
    public DepartmentResponse updateDepartment(Integer deptId, UpdateDepartmentRequest updateDepartmentRequest) {
        Long userId = securityUtils.getCurrentUserId();
        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new BadRequestExceptions("Department not found!"));

        if(updateDepartmentRequest.getDeptName() != null){
            dept.setDeptName(updateDepartmentRequest.getDeptName());
        }

        if(updateDepartmentRequest.getActive() != null){
            dept.setActive(updateDepartmentRequest.getActive());
        }

        dept.setUpdatedAt(OffsetDateTime.now());
        dept.setUpdatedBy(userId);

        Department savedDept = departmentRepository.save(dept);

        return toResponse(savedDept);
    }

    @Async("taskExecutor")
    private DepartmentResponse toResponse(Department department){

        String createdByUser = userRepository.findByUserId(department.getCreatedBy())
                .map(User::getFirstName)
                .orElse(null);

        String updatedByUser = userRepository.findByUserId(department.getUpdatedBy())
                .map(User::getFirstName)
                .orElse(null);

        return DepartmentResponse.builder()
                .deptId(department.getDeptId())
                .deptName(department.getDeptName())
                .createdAt(department.getCreatedAt())
                .createdBy(createdByUser)
                .updatedAt(department.getUpdatedAt())
                .updatedBy(updatedByUser)
                .active(department.getActive())
                .build();
    }
}
