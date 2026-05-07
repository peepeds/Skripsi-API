package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanyRequestRepository extends JpaRepository<CompanyRequest, Long> {
    boolean existsByCompanyNameIgnoreCaseAndStatus(String companyName, CompanyRequestStatus status);

    long countByStatus(CompanyRequestStatus status);

    Page<CompanyRequest> findByStatus(CompanyRequestStatus status, Pageable pageable);

    Page<CompanyRequest> findByCreatedBy(Long createdBy, Pageable pageable);

    Page<CompanyRequest> findByCreatedByOrderByUpdatedAtDesc(Long createdBy, Pageable pageable);

    @Query("SELECT cr FROM CompanyRequest cr WHERE (:cursor IS NULL OR cr.companyRequestId > :cursor) ORDER BY cr.companyRequestId ASC")
    List<CompanyRequest> findPageFromCursor(@Param("cursor") Long cursor, Pageable pageable);

    @Query("SELECT cr FROM CompanyRequest cr WHERE cr.status = :status AND (:cursor IS NULL OR cr.companyRequestId > :cursor) ORDER BY cr.companyRequestId ASC")
    List<CompanyRequest> findPageByStatusFromCursor(@Param("status") CompanyRequestStatus status, @Param("cursor") Long cursor, Pageable pageable);
}

