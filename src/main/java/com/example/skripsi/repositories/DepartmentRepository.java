package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department,Integer> {
    List<Department> findAll();
    boolean existsByDeptNameIgnoreCase(String deptName);

}
