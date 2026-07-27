package com.example.skripsi.controllers;

import com.example.skripsi.interfaces.IDepartmentService;
import com.example.skripsi.models.department.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("department")
public class DepartmentController extends AbstractMasterDataController<IDepartmentService, DepartmentResponse, CreateDepartmentRequest, UpdateDepartmentRequest> {

    public DepartmentController(IDepartmentService departmentService) {
        super(departmentService);
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
