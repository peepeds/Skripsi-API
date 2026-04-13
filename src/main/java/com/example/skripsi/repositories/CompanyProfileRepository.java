package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {
    CompanyProfile findByCompanyId(Long companyId);

    @Query("SELECT cp FROM CompanyProfile cp WHERE cp.companyId IN :companyIds")
    List<CompanyProfile> findByCompanyIds(@Param("companyIds") List<Long> companyIds);

    @Query(value = "SELECT COUNT(*) FROM company_profiles WHERE subcategory_id = :subCategoryId", nativeQuery = true)
    Long countBySubcategoryId(@Param("subCategoryId") Long subCategoryId);

    @Query(value = "SELECT COUNT(*) FROM company_profiles WHERE subcategory_id = :subCategoryId AND is_partner = true", nativeQuery = true)
    Long countPartnersBySubcategoryId(@Param("subCategoryId") Long subCategoryId);

    @Query(value = """
            SELECT COUNT(DISTINCT ih.company_id)
            FROM internship_headers ih
            INNER JOIN internship_job_subcategories ijsc ON ih.internship_header_id = ijsc.internship_header_id
            WHERE ijsc.sub_category_id = :subCategoryId
            """, nativeQuery = true)
    Long countCompaniesByJobSubcategoryId(@Param("subCategoryId") Long subCategoryId);

    @Query(value = """
            SELECT COUNT(DISTINCT ih.company_id)
            FROM internship_headers ih
            INNER JOIN internship_job_subcategories ijsc ON ih.internship_header_id = ijsc.internship_header_id
            INNER JOIN company_profiles cp ON ih.company_id = cp.company_id
            WHERE ijsc.sub_category_id = :subCategoryId AND cp.is_partner = true
            """, nativeQuery = true)
    Long countPartnerCompaniesByJobSubcategoryId(@Param("subCategoryId") Long subCategoryId);
}

