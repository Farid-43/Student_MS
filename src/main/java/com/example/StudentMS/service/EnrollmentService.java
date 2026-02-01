package com.example.StudentMS.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.StudentMS.dto.EnrollmentDTO;
import com.example.StudentMS.entity.Course;
import com.example.StudentMS.entity.Enrollment;
import com.example.StudentMS.entity.Student;
import com.example.StudentMS.repository.CourseRepository;
import com.example.StudentMS.repository.EnrollmentRepository;
import com.example.StudentMS.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public List<EnrollmentDTO> getEnrollmentsByStudentId(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<EnrollmentDTO> getEnrollmentsByCourseId(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EnrollmentDTO enrollStudent(Long studentId, Long courseId) {
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new RuntimeException("Student is already enrolled in this course");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .enrolledDate(LocalDate.now())
                .status("ENROLLED")
                .build();

        return toDTO(enrollmentRepository.save(enrollment));
    }

    public void dropEnrollment(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        enrollmentRepository.delete(enrollment);
    }

    public void deleteEnrollment(Long id) {
        enrollmentRepository.deleteById(id);
    }

    private EnrollmentDTO toDTO(Enrollment enrollment) {
        return EnrollmentDTO.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .studentName(enrollment.getStudent().getUser().getUsername())
                .courseId(enrollment.getCourse().getId())
                .courseCode(enrollment.getCourse().getCourseCode())
                .courseName(enrollment.getCourse().getCourseName())
                .enrolledDate(enrollment.getEnrolledDate())
                .status(enrollment.getStatus())
                .build();
    }
}
