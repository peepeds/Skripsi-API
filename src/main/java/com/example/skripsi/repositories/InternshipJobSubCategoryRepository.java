package com.example.skripsi.repositories;

import com.example.skripsi.entities.InternshipJobSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipJobSubCategoryRepository extends JpaRepository<InternshipJobSubCategory, Long> {
    List<InternshipJobSubCategory> findByInternshipHeaderId(Long internshipHeaderId);
}
