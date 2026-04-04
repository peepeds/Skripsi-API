package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternshipHeaderRepository extends JpaRepository<InternshipHeader, Long> {
    List<InternshipHeader> findByUserId(Long userId);
    List<InternshipHeader> findByCompanyId(Long companyId);
    Optional<InternshipHeader> findByUserIdAndCompanyId(Long userId, Long companyId);

    @Query("SELECT DISTINCT ih.jobTitle FROM InternshipHeader ih WHERE LOWER(ih.jobTitle) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY ih.jobTitle")
    List<String> searchJobTitles(@Param("query") String query);

    @Query("SELECT ih FROM InternshipHeader ih WHERE ih.internshipHeaderId IN :headerIds")
    List<InternshipHeader> findByInternshipHeaderIds(@Param("headerIds") List<Long> headerIds);

    @Query("SELECT ih FROM InternshipHeader ih WHERE ih.companyId = :companyId AND (:cursor IS NULL OR ih.internshipHeaderId < :cursor) ORDER BY ih.internshipHeaderId DESC")
    List<InternshipHeader> findPageByCompanyIdDesc(@Param("companyId") Long companyId, @Param("cursor") Long cursor, Pageable pageable);

    @Query("SELECT ih FROM InternshipHeader ih WHERE ih.companyId = :companyId AND (:cursor IS NULL OR ih.internshipHeaderId > :cursor) ORDER BY ih.internshipHeaderId ASC")
    List<InternshipHeader> findPageByCompanyIdAsc(@Param("companyId") Long companyId, @Param("cursor") Long cursor, Pageable pageable);
}
