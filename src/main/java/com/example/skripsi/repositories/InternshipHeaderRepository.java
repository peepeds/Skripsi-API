package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
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
}
