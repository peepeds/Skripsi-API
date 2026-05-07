package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import com.example.skripsi.models.major.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MajorRepository extends JpaRepository<Major,Integer> {
    List<Major> findAll();

    @Query("SELECT m FROM Major m JOIN FETCH m.department JOIN FETCH m.region WHERE m.active = true")
    List<Major> findAllByActiveTrue();

    boolean existsByMajorNameIgnoreCaseAndActiveTrue(String majorName);
}
