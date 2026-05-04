package com.example.skripsi.controllers;

import com.example.skripsi.models.*;
import com.example.skripsi.models.major.*;
import com.example.skripsi.services.MajorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("major")
public class MajorController extends AbstractMasterDataController<MajorService, MajorResponse, CreateMajorRequest, UpdateMajorRequest> {

    public MajorController(MajorService majorService) {
        super(majorService);
    }

    @GetMapping("/options")
    public ResponseEntity<Map<String, Object>> getAllMajorOptions() {
        var results = service.getAllMajorOptions();
        return ResponseEntity.ok(Map.of("success", true, "message", "OK", "result", results));
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
