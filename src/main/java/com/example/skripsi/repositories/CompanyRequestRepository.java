package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRequestRepository extends JpaRepository<CompanyRequest, Long> {
    boolean existsByCompanyNameIgnoreCaseAndStatus(String companyName, CompanyRequestStatus status);

    Page<CompanyRequest> findByStatus(CompanyRequestStatus status, Pageable pageable);

    Page<CompanyRequest> findByCreatedBy(Long createdBy, Pageable pageable);

    Page<CompanyRequest> findByCreatedByOrderByUpdatedAtDesc(Long createdBy, Pageable pageable);
}

