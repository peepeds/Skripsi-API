package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import com.example.skripsi.repositories.projections.CompanyRatingProjection;
import com.example.skripsi.repositories.projections.CompanyReviewCountProjection;
import com.example.skripsi.repositories.projections.RecentReviewProjection;
import com.example.skripsi.repositories.projections.SubCategorySummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternshipDetailRepository extends JpaRepository<InternshipDetail, Long> {
    Optional<InternshipDetail> findByInternshipHeaderId(Long internshipHeaderId);

    @Query("SELECT id FROM InternshipDetail id WHERE id.internshipHeaderId IN (SELECT ih.internshipHeaderId FROM InternshipHeader ih WHERE ih.companyId = :companyId)")
    List<InternshipDetail> findAllDetailsByCompanyId(@Param("companyId") Long companyId);

    @Query(value = "SELECT ih.company_id as companyId, " +
            "ROUND(AVG((id.work_culture_rating + id.learning_opportunity_rating + id.mentorship_rating + id.benefits_rating + id.work_life_balance_rating) / 5.0), 1) as avgRating " +
            "FROM internship_details id " +
            "INNER JOIN internship_headers ih ON id.internship_header_id = ih.internship_header_id " +
            "WHERE ih.company_id IN :companyIds " +
            "GROUP BY ih.company_id", nativeQuery = true)
    List<CompanyRatingProjection> findAverageRatingsByCompanyIds(@Param("companyIds") List<Long> companyIds);

    @Query(value = "SELECT COUNT(DISTINCT ih.internship_header_id) " +
            "FROM internship_headers ih " +
            "WHERE ih.company_id = :companyId", nativeQuery = true)
    Long findReviewCountByCompanyId(@Param("companyId") Long companyId);

    @Query(value = """
            SELECT ih.company_id as companyId, COUNT(DISTINCT ih.internship_header_id) as reviewCount
            FROM internship_headers ih
            WHERE ih.company_id IN :companyIds
            GROUP BY ih.company_id
            """, nativeQuery = true)
    List<CompanyReviewCountProjection> findReviewCountsByCompanyIds(@Param("companyIds") List<Long> companyIds);

    @Query("SELECT id FROM InternshipDetail id WHERE id.internshipHeaderId IN :headerIds")
    List<InternshipDetail> findByInternshipHeaderIds(@Param("headerIds") List<Long> headerIds);

    @Query(value = "SELECT ih.company_id as companyId, " +
            "ROUND(AVG((id.work_culture_rating + id.learning_opportunity_rating + id.mentorship_rating + id.benefits_rating + id.work_life_balance_rating) / 5.0), 1) as avgRating " +
            "FROM internship_details id " +
            "INNER JOIN internship_headers ih ON id.internship_header_id = ih.internship_header_id " +
            "GROUP BY ih.company_id " +
            "ORDER BY avgRating DESC " +
            "LIMIT 10", nativeQuery = true)
    List<CompanyRatingProjection> findTop10CompaniesByAverageRating();

    @Query(value = "SELECT id.testimony, " +
            "CONCAT(u.first_name, ' ', u.last_name) as createdByName, " +
            "ROUND(((COALESCE(id.work_culture_rating, 0) + COALESCE(id.learning_opportunity_rating, 0) + COALESCE(id.mentorship_rating, 0) + COALESCE(id.benefits_rating, 0) + COALESCE(id.work_life_balance_rating, 0)) / 5.0), 1) as averageRating, " +
            "c.company_name as companyName, " +
            "cat.category_name as companyCategory, " +
            "sc.sub_category_name as companySubCategory, " +
            "cp.website as companyWebsite, " +
            "ih.job_title as jobTitle, " +
            "id.created_at as createdAt " +
            "FROM internship_details id " +
            "INNER JOIN internship_headers ih ON id.internship_header_id = ih.internship_header_id " +
            "INNER JOIN users u ON id.created_by = u.user_id " +
            "INNER JOIN companies c ON ih.company_id = c.company_id " +
            "LEFT JOIN company_profiles cp ON c.company_id = cp.company_id " +
            "LEFT JOIN sub_categories sc ON cp.subcategory_id = sc.sub_category_id " +
            "LEFT JOIN categories cat ON sc.category_id = cat.category_id " +
            "ORDER BY id.created_at DESC " +
            "LIMIT 10", nativeQuery = true)
    List<RecentReviewProjection> findTop10RecentReviews();

    @Query(value = """
            SELECT
                COUNT(DISTINCT id.internship_detail_id) as totalReviews,
                ROUND(AVG((id.work_culture_rating + id.learning_opportunity_rating + id.mentorship_rating + id.benefits_rating + id.work_life_balance_rating) / 5.0), 1) as avgRating
            FROM internship_details id
            INNER JOIN internship_headers ih ON id.internship_header_id = ih.internship_header_id
            INNER JOIN company_profiles cp ON ih.company_id = cp.company_id
            WHERE cp.subcategory_id = :subCategoryId
              AND id.work_culture_rating IS NOT NULL
            """, nativeQuery = true)
    SubCategorySummaryProjection findSummaryBySubCategoryId(@Param("subCategoryId") Long subCategoryId);

    @Query(value = """
            SELECT
                COUNT(DISTINCT id.internship_detail_id) as totalReviews,
                ROUND(AVG((id.work_culture_rating + id.learning_opportunity_rating + id.mentorship_rating + id.benefits_rating + id.work_life_balance_rating) / 5.0), 1) as avgRating
            FROM internship_details id
            INNER JOIN internship_headers ih ON id.internship_header_id = ih.internship_header_id
            INNER JOIN internship_job_subcategories ijsc ON ih.internship_header_id = ijsc.internship_header_id
            WHERE ijsc.sub_category_id = :subCategoryId
              AND id.work_culture_rating IS NOT NULL
            """, nativeQuery = true)
    SubCategorySummaryProjection findSummaryByJobSubCategoryId(@Param("subCategoryId") Long subCategoryId);
}
