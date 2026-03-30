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
}

