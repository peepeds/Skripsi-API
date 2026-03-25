package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruitmentStepRepository extends JpaRepository<RecruitmentStep, Long> {
    List<RecruitmentStep> findByInternshipHeaderId(Long internshipHeaderId);
}
