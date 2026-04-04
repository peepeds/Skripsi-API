package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruitmentStepRepository extends JpaRepository<RecruitmentStep, Long> {
    List<RecruitmentStep> findByInternshipHeaderId(Long internshipHeaderId);

    @Query("SELECT rs FROM RecruitmentStep rs WHERE rs.internshipHeaderId IN (SELECT ih.internshipHeaderId FROM InternshipHeader ih WHERE ih.companyId = :companyId)")
    List<RecruitmentStep> findAllStepsByCompanyId(@Param("companyId") Long companyId);

    List<RecruitmentStep> findByInternshipHeaderIdIn(List<Long> headerIds);
}
