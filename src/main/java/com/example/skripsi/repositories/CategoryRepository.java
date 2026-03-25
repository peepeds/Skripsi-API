package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subCategories")
    List<Category> findAllWithSubCategories();

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subCategories WHERE c.categoryType = :categoryType")
    List<Category> findByCategoryTypeWithSubCategories(String categoryType);

    List<Category> findByCategoryType(String categoryType);
}
