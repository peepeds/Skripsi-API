package com.example.skripsi.repositories;

import com.example.skripsi.entities.Company;
import com.example.skripsi.models.company.CompanyOptionsResponse;
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
}
