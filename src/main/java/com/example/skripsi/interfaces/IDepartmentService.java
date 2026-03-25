package com.example.skripsi.interfaces;

import com.example.skripsi.models.department.*;

import java.util.List;

public interface IDepartmentService {
    List<DepartmentResponse> getAllDepartment();
    DepartmentResponse createDepartment(CreateDepartmentRequest createDepartmentRequest);
    DepartmentResponse updateDepartment(Integer deptId, UpdateDepartmentRequest updateDepartmentRequest);
}
