package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import com.example.skripsi.models.major.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MajorRepository extends JpaRepository<Major,Integer> {
    List<Major> findAll();
    @Query("SELECT new com.example.skripsi.models.major.MajorOptionResponse(m.region.regionId, m.majorId, m.majorName) " +
            "FROM Major m ")
    List<MajorOptionResponse> findAllOptions();
    boolean existsByMajorNameIgnoreCase(String majorName);
}
