package com.example.skripsi.repositories;

import com.example.skripsi.entities.CompanySave;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanySaveRepository extends JpaRepository<CompanySave, Long> {
    Optional<CompanySave> findByUserIdAndCompanyId(Long userId, Long companyId);

    @Query("""
        SELECT cs FROM CompanySave cs
        WHERE cs.userId = :userId
          AND cs.isSave = true
          AND (:cursor IS NULL OR cs.companySaveId > :cursor)
        ORDER BY cs.companySaveId ASC
    """)
    List<CompanySave> findBookmarksByUserIdFromCursor(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
}
