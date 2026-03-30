package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.department.*;
import com.example.skripsi.repositories.*;
import com.example.skripsi.securities.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DepartmentService extends AbstractMasterDataService<Department, DepartmentResponse, CreateDepartmentRequest, UpdateDepartmentRequest> implements IDepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository,
                             UserRepository userRepository,
                             SecurityUtils securityUtils) {
        super(departmentRepository, userRepository, securityUtils);
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentResponse> getAllDepartment() {
        return getAll();
    }

    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        return create(request);
    }

    public DepartmentResponse updateDepartment(Integer deptId, UpdateDepartmentRequest request) {
        return update(deptId, request);
    }

    @Override
    protected void validateBeforeCreate(CreateDepartmentRequest request) {
        boolean isExists = departmentRepository.existsByDeptNameIgnoreCase(request.getDeptName().trim());
        if (isExists) {
            throw new BadRequestExceptions("Department Name already exists!");
        }
    }

    @Override
    protected Department buildEntity(CreateDepartmentRequest request, Long userId) {
        return Department.builder()
                .deptName(request.getDeptName())
                .createdAt(OffsetDateTime.now())
                .createdBy(userId)
                .active(true)
                .build();
    }

    @Override
    protected void updateEntityFields(Department entity, UpdateDepartmentRequest request) {
        if (request.getDeptName() != null) {
            entity.setDeptName(request.getDeptName());
        }
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }
    }

    @Override
    protected Department setUpdateAuditFields(Department entity, Long userId) {
        entity.setUpdatedAt(OffsetDateTime.now());
        entity.setUpdatedBy(userId);
        return entity;
    }

    @Override
    protected DepartmentResponse toResponse(Department department, Map<Long, User> userMap) {
        String createdByUser = resolveUsername(department.getCreatedBy(), userMap);
        String updatedByUser = resolveUsername(department.getUpdatedBy(), userMap);

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

    @Override
    protected String getNotFoundErrorMessage() {
        return "Department not found!";
    }
}
