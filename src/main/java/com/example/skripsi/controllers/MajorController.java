package com.example.skripsi.controllers;

import com.example.skripsi.models.*;
import com.example.skripsi.models.major.*;
import com.example.skripsi.services.MajorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("major")
public class MajorController extends AbstractMasterDataController<MajorService, MajorResponse, CreateMajorRequest, UpdateMajorRequest> {

    public MajorController(MajorService majorService) {
        super(majorService);
    }

    @GetMapping("/options")
    public WebResponse<?> getAllMajorOptions() {
        var results = service.getAllMajorOptions();
        return WebResponse.builder()
                .success(true)
                .message("Successfully Get Major")
                .result(results)
                .build();
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
