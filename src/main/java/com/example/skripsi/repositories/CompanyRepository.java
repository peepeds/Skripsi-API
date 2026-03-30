package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import com.example.skripsi.models.company.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Query("""
        SELECT new com.example.skripsi.models.company.CompanyOptionsResponse(
            c.companyId,
            c.companyName,
            c.companyAbbreviation,
            c.companySlug
        )
        FROM Company c
        WHERE (:cursor IS NULL OR c.companyId > :cursor)
        ORDER BY c.companyId
    """)
    List<CompanyOptionsResponse> getCompanyFromCursor(
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    boolean existsByCompanyNameIgnoreCase(String companyName);

    @Query("""
        SELECT new com.example.skripsi.models.company.CompanyOptionsResponse(
            c.companyId,
            c.companyName,
            c.companyAbbreviation,
            c.companySlug
        )
        FROM Company c
        WHERE LOWER(c.companyName) LIKE LOWER(CONCAT(:search, '%'))
            OR LOWER(c.companyAbbreviation) LIKE LOWER(CONCAT(:search, '%'))
        ORDER BY c.companyName ASC
    """)
    List<CompanyOptionsResponse> searchCompanies(@Param("search") String search);

    Company findByCompanySlug(String companySlug);

    @Query("""
        SELECT DISTINCT new com.example.skripsi.models.company.CompanyOptionsResponse(
            c.companyId,
            c.companyName,
            c.companyAbbreviation,
            c.companySlug
        )
        FROM Company c
        JOIN InternshipHeader ih ON ih.company = c
        JOIN InternshipJobSubCategory ijs ON ijs.internshipHeaderId = ih.internshipHeaderId
        WHERE ijs.subCategoryId = :subCategoryId
        ORDER BY c.companyName ASC
    """)
    Page<CompanyOptionsResponse> findCompaniesBySubCategoryId(
            @Param("subCategoryId") Long subCategoryId,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT new com.example.skripsi.models.company.CompanyOptionsResponse(
            c.companyId,
            c.companyName,
            c.companyAbbreviation,
            c.companySlug
        )
        FROM Company c
        JOIN CompanyProfile cp ON cp.companyId = c.companyId
        WHERE cp.subcategoryId = :subCategoryId
        ORDER BY c.companyName ASC
    """)
    Page<CompanyOptionsResponse> findCompaniesBySubCategoryIdViaProfile(
            @Param("subCategoryId") Long subCategoryId,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT new com.example.skripsi.models.company.CompanyOptionsResponse(
            c.companyId,
            c.companyName,
            c.companyAbbreviation,
            c.companySlug
        )
        FROM Company c
        JOIN CompanyProfile cp ON cp.companyId = c.companyId
        JOIN SubCategory sc ON sc.subCategoryId = cp.subcategoryId
        WHERE LOWER(sc.subCategoryName) = LOWER(:subCategoryName)
        ORDER BY c.companyName ASC
    """)
    Page<CompanyOptionsResponse> findCompaniesBySubCategoryNameViaProfile(
            @Param("subCategoryName") String subCategoryName,
            Pageable pageable
    );
}
