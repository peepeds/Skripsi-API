package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InternshipDetailRepository extends JpaRepository<InternshipDetail, Long> {
    Optional<InternshipDetail> findByInternshipHeaderId(Long internshipHeaderId);
}
