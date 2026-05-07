package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import com.example.skripsi.repositories.projections.TopSubCategoryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {
    @Query("SELECT sc FROM SubCategory sc JOIN FETCH sc.category WHERE LOWER(sc.subCategoryName) = LOWER(:name)")
    Optional<SubCategory> findBySubCategoryNameIgnoreCase(@Param("name") String name);

    @Query("SELECT sc FROM SubCategory sc JOIN FETCH sc.category WHERE sc.subCategoryId IN :subCategoryIds")
    List<SubCategory> findBySubCategoryIds(@Param("subCategoryIds") List<Long> subCategoryIds);

    @Query("SELECT sc FROM SubCategory sc JOIN FETCH sc.category")
    List<SubCategory> findAllWithCategory();

    @Query(value = """
            SELECT sc.sub_category_name AS subCategoryName, COUNT(*) AS totalReviews
            FROM internship_job_subcategories ijsc
            JOIN sub_categories sc ON sc.sub_category_id = ijsc.sub_category_id
            GROUP BY ijsc.sub_category_id, sc.sub_category_name
            ORDER BY COUNT(*) DESC
            LIMIT 10
            """, nativeQuery = true)
    List<TopSubCategoryProjection> findTop10SubCategoryNames();
}

