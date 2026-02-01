package com.example.StudentMS.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.StudentMS.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByDeptCode(String deptCode);

    Optional<Department> findByDeptName(String deptName);

    boolean existsByDeptCode(String deptCode);
}
