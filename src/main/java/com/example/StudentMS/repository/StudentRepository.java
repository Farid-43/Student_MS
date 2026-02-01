package com.example.StudentMS.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.StudentMS.entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentId(String studentId);

    Optional<Student> findByUserId(Long userId);

    Boolean existsByStudentId(String studentId);

    List<Student> findByDepartment(String department);
}
