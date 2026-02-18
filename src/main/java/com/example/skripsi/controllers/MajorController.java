package com.example.skripsi.controllers;

import com.example.skripsi.interfaces.IMajorService;
import com.example.skripsi.models.WebResponse;
import com.example.skripsi.models.major.CreateMajorRequest;
import com.example.skripsi.models.major.UpdateMajorRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("major")
public class MajorController {

    private final IMajorService majorService;

    public MajorController(IMajorService majorService){
        this.majorService = majorService;
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public WebResponse<Object> getAllMajor(){

        var results = majorService.getAllMajor();

        return  WebResponse.builder()
                .success(true)
                .message("successfully Get All Major")
                .result(results)
                .build();
    }

    @PostMapping("")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public WebResponse<?> createMajor(@Valid @RequestBody CreateMajorRequest createMajorRequest){

        var result =  majorService.createMajor(createMajorRequest);
        return WebResponse.builder()
                .success(true)
                .message("Successfully Created New Major")
                .result(result)
                .build();
    }

    @PatchMapping("/{majorId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public WebResponse<?> updateMajor(@Valid @RequestBody UpdateMajorRequest updateMajorRequest, @PathVariable Integer majorId){

        var result = majorService.updateMajor(majorId, updateMajorRequest);

        return WebResponse.builder()
                .success(true)
                .message("Successfully Updated Major")
                .result(result)
                .build();
    }

    @GetMapping("/options")
    public WebResponse<?>getAllMajorOptions(){

        var results = majorService.getAllMajorOptions();

        return  WebResponse.builder()
                .success(true)
                .message("Successfully Get Major")
                .result(results)
                .build();
    }
}
