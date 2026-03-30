package com.example.skripsi.repositories;

import com.example.skripsi.entities.InternshipJobSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipJobSubCategoryRepository extends JpaRepository<InternshipJobSubCategory, Long> {
    List<InternshipJobSubCategory> findByInternshipHeaderId(Long internshipHeaderId);

    @Query("SELECT ijsc FROM InternshipJobSubCategory ijsc WHERE ijsc.internshipHeaderId IN (SELECT ih.internshipHeaderId FROM InternshipHeader ih WHERE ih.companyId = :companyId)")
    List<InternshipJobSubCategory> findAllSubCategoriesByCompanyId(@Param("companyId") Long companyId);
}
