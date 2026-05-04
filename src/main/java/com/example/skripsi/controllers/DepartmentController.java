package com.example.skripsi.controllers;

import com.example.skripsi.models.WebResponse;
import com.example.skripsi.models.department.*;
import com.example.skripsi.services.DepartmentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("department")
public class DepartmentController extends AbstractMasterDataController<DepartmentService, DepartmentResponse, CreateDepartmentRequest, UpdateDepartmentRequest> {

    public DepartmentController(DepartmentService departmentService) {
        super(departmentService);
    }

    @GetMapping("/options")
    public WebResponse<?> getAllDepartmentOptions() {
        var results = service.getAllDepartment();
        return WebResponse.builder()
                .success(true)
                .message("Successfully Get Departments")
                .result(results)
                .build();
    }

    @Override
    protected String getGetAllMessage() {
        return "Successfully Get All Department";
    }

    @Override
    protected String getCreateMessage() {
        return "Successfully Create Department";
    }

    @Override
    protected String getUpdateMessage() {
        return "Successfully Update Department";
    }
}
