package com.example.skripsi.controllers;

import com.example.skripsi.interfaces.IDepartmentService;
import com.example.skripsi.models.WebResponse;
import com.example.skripsi.models.department.CreateDepartmentRequest;
import com.example.skripsi.models.department.UpdateDepartmentRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("department")
public class DepartmentController {
    private final IDepartmentService departmentService;

    public DepartmentController(IDepartmentService departmentService){
        this.departmentService = departmentService;
    }


    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public WebResponse<?> getAllDepartment(){

        var results = departmentService.getAllDepartment();

        return WebResponse.builder()
                        .success(true)
                        .message("Successfully Get All Department")
                        .result(results)
                        .build();
    }

    @PostMapping("")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public WebResponse<?> createDepartment(@Valid @RequestBody CreateDepartmentRequest createDepartmentRequest){
        var results = departmentService.createDepartment(createDepartmentRequest);

        return WebResponse.builder()
                .success(true)
                .message("Successfully Create Department")
                .result(results)
                .build();
    }

    @PatchMapping("{deptId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public WebResponse<?> updateDepartment(@Valid @RequestBody UpdateDepartmentRequest updateDepartmentRequest, @PathVariable Integer deptId){

        var result = departmentService.updateDepartment(deptId, updateDepartmentRequest);

        return WebResponse.builder()
                .success(true)
                .message("Successfully Update Department")
                .result(result)
                .build();
    }

}
