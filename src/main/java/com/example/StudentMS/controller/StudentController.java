package com.example.StudentMS.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.StudentMS.dto.CourseDTO;
import com.example.StudentMS.dto.EnrollmentDTO;
import com.example.StudentMS.dto.StudentDTO;
import com.example.StudentMS.entity.Student;
import com.example.StudentMS.entity.User;
import com.example.StudentMS.repository.StudentRepository;
import com.example.StudentMS.repository.UserRepository;
import com.example.StudentMS.service.CourseService;
import com.example.StudentMS.service.EnrollmentService;
import com.example.StudentMS.service.StudentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
public class StudentController {

    private final StudentService studentService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    // ================== VIEW OWN PROFILE ==================

    @GetMapping("/profile")
    public ResponseEntity<StudentDTO> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        return ResponseEntity.ok(studentService.getStudentById(student.getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<StudentDTO> updateMyProfile(@RequestBody StudentDTO studentDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        // Students can only update limited fields
        StudentDTO existingStudent = studentService.getStudentById(student.getId());
        existingStudent.setFirstName(studentDTO.getFirstName());
        existingStudent.setLastName(studentDTO.getLastName());
        // Don't allow changing GPA, department, etc.

        return ResponseEntity.ok(studentService.updateStudent(student.getId(), existingStudent));
    }

    // ================== VIEW COURSES ==================

    @GetMapping("/courses")
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @GetMapping("/courses/by-code/{courseCode}")
    public ResponseEntity<CourseDTO> getCourseByCourseCode(@PathVariable String courseCode) {
        return ResponseEntity.ok(courseService.getCourseByCourseCode(courseCode));
    }

    // ================== ENROLLMENT ==================

    @GetMapping("/my-enrollments")
    public ResponseEntity<List<EnrollmentDTO>> getMyEnrollments() {
        Student student = getCurrentStudent();
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudentId(student.getId()));
    }

    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<EnrollmentDTO> enrollInCourse(@PathVariable Long courseId) {
        Student student = getCurrentStudent();
        return ResponseEntity.ok(enrollmentService.enrollStudent(student.getId(), courseId));
    }

    @DeleteMapping("/drop/{courseId}")
    public ResponseEntity<?> dropCourse(@PathVariable Long courseId) {
        Student student = getCurrentStudent();
        enrollmentService.dropEnrollment(student.getId(), courseId);
        return ResponseEntity.ok().build();
    }

    // ================== DEPARTMENT STUDENTS ==================

    @GetMapping("/department-students")
    public ResponseEntity<List<StudentDTO>> getDepartmentStudents() {
        Student student = getCurrentStudent();
        String department = student.getDepartment();
        if (department == null || department.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(studentService.getStudentsByDepartment(department));
    }

    private Student getCurrentStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student profile not found"));
    }
}
