package com.example.skripsi.controllers;

import com.example.skripsi.interfaces.IMajorService;
import com.example.skripsi.models.major.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("major")
public class MajorController extends AbstractMasterDataOptionsController<IMajorService, MajorResponse, CreateMajorRequest, UpdateMajorRequest> {

    public MajorController(IMajorService majorService) {
        super(majorService, majorService::getAllMajorOptions, "Successfully Get Major");
    }

    @Override
    protected String getGetAllMessage() {
        return "successfully Get All Major";
    }

    @Override
    protected String getCreateMessage() {
        return "Successfully Created New Major";
    }

    @Override
    protected String getUpdateMessage() {
        return "Successfully Updated Major";
    }
}
