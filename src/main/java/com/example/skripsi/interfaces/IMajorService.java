package com.example.skripsi.interfaces;

import com.example.skripsi.entities.Major;
import com.example.skripsi.models.major.*;

import java.util.List;

public interface IMajorService extends IMasterDataService<MajorResponse, CreateMajorRequest, UpdateMajorRequest> {
    List<MajorResponse> getAllMajor();
    List<MajorOptionResponse> getAllMajorOptions();
    MajorResponse createMajor(CreateMajorRequest createMajorRequest);
    MajorResponse updateMajor(Integer majorId, UpdateMajorRequest updateMajorRequest);
    Major findMajorById(Long id);
}
