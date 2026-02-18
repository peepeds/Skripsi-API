package com.example.skripsi.interfaces;

import com.example.skripsi.models.major.CreateMajorRequest;
import com.example.skripsi.models.major.MajorResponse;
import com.example.skripsi.models.major.MajorOptionResponse;
import com.example.skripsi.models.major.UpdateMajorRequest;

import java.util.concurrent.CompletableFuture;

import java.util.List;

public interface IMajorService {
    public List<MajorResponse> getAllMajor();
    public List<MajorOptionResponse> getAllMajorOptions();
    public MajorResponse createMajor(CreateMajorRequest createMajorRequest);
    public MajorResponse updateMajor(Integer majorId, UpdateMajorRequest updateMajorRequest);
}
