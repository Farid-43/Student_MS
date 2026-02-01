package com.example.StudentMS.repository;

import com.example.StudentMS.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByEmployeeId(String employeeId);
    Optional<Teacher> findByUserId(Long userId);
    Boolean existsByEmployeeId(String employeeId);
}
