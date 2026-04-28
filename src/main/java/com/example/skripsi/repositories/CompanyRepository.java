package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import com.example.skripsi.models.company.*;
import com.example.skripsi.repositories.projections.MonthlyCountProjection;
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

    @Query("""
        SELECT c FROM Company c
        WHERE (:cursor IS NULL OR c.companyId > :cursor)
        ORDER BY c.companyId ASC
    """)
    List<Company> findPageFromCursor(@Param("cursor") Long cursor, Pageable pageable);

    @Query("""
        SELECT c FROM Company c
        WHERE (:cursor IS NULL OR c.companyId < :cursor)
        ORDER BY c.companyId DESC
    """)
    List<Company> findPageFromCursorLatest(@Param("cursor") Long cursor, Pageable pageable);

    @Query(value = """
        WITH company_ratings AS (
            SELECT ih.company_id,
                   ROUND(AVG((id.work_culture_rating + id.learning_opportunity_rating + id.mentorship_rating + id.benefits_rating + id.work_life_balance_rating) / 5.0), 1) AS avg_rating
            FROM internship_details id
            INNER JOIN internship_headers ih ON id.internship_header_id = ih.internship_header_id
            GROUP BY ih.company_id
        )
        SELECT c.* FROM companies c
        LEFT JOIN company_ratings cr ON cr.company_id = c.company_id
        WHERE :cursor IS NULL
           OR (COALESCE(cr.avg_rating, 0) < (
                   SELECT COALESCE(cr2.avg_rating, 0) FROM company_ratings cr2 WHERE cr2.company_id = :cursor))
           OR (COALESCE(cr.avg_rating, 0) = (
                   SELECT COALESCE(cr2.avg_rating, 0) FROM company_ratings cr2 WHERE cr2.company_id = :cursor)
               AND c.company_id > :cursor)
        ORDER BY COALESCE(cr.avg_rating, 0) DESC, c.company_id ASC
    """, nativeQuery = true)
    List<Company> findPageFromCursorTop(@Param("cursor") Long cursor, Pageable pageable);

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
          AND (:cursor IS NULL OR c.companyId > :cursor)
        ORDER BY c.companyId ASC
    """)
    List<CompanyOptionsResponse> findCompaniesBySubCategoryIdFromCursor(
            @Param("subCategoryId") Long subCategoryId,
            @Param("cursor") Long cursor,
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
          AND (:cursor IS NULL OR c.companyId > :cursor)
        ORDER BY c.companyId ASC
    """)
    List<CompanyOptionsResponse> findCompaniesBySubCategoryIdViaProfileFromCursor(
            @Param("subCategoryId") Long subCategoryId,
            @Param("cursor") Long cursor,
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
          AND (:cursor IS NULL OR c.companyId > :cursor)
        ORDER BY c.companyId ASC
    """)
    List<CompanyOptionsResponse> findCompaniesBySubCategoryNameViaProfileFromCursor(
            @Param("subCategoryName") String subCategoryName,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query(value = """
            SELECT TO_CHAR(DATE_TRUNC('month', c.created_at), 'YYYY-MM') AS month,
                   COUNT(*) AS count
            FROM companies c
            WHERE c.created_at >= DATE_TRUNC('month', NOW()) - INTERVAL '5 months'
            GROUP BY DATE_TRUNC('month', c.created_at)
            ORDER BY DATE_TRUNC('month', c.created_at) ASC
            """, nativeQuery = true)
    List<MonthlyCountProjection> countNewCompaniesPerMonthLast6Months();
}
