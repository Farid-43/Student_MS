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
import com.example.StudentMS.dto.DepartmentDTO;
import com.example.StudentMS.dto.MessageResponse;
import com.example.StudentMS.dto.StudentDTO;
import com.example.StudentMS.entity.Teacher;
import com.example.StudentMS.entity.User;
import com.example.StudentMS.repository.TeacherRepository;
import com.example.StudentMS.repository.UserRepository;
import com.example.StudentMS.service.CourseService;
import com.example.StudentMS.service.DepartmentService;
import com.example.StudentMS.service.StudentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class TeacherController {

    private final StudentService studentService;
    private final CourseService courseService;
    private final DepartmentService departmentService;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;

    // ================== VIEW STUDENTS ==================

    @GetMapping("/students")
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @GetMapping("/students/by-student-id/{studentId}")
    public ResponseEntity<StudentDTO> getStudentByStudentId(@PathVariable String studentId) {
        return ResponseEntity.ok(studentService.getStudentByStudentId(studentId));
    }

    // Teachers can update student info (like grades)
    @PutMapping("/students/{id}")
    public ResponseEntity<StudentDTO> updateStudent(@PathVariable Long id, @RequestBody StudentDTO studentDTO) {
        return ResponseEntity.ok(studentService.updateStudent(id, studentDTO));
    }

    // ================== COURSE MANAGEMENT ==================

    @GetMapping("/courses")
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @GetMapping("/my-courses")
    public ResponseEntity<List<CourseDTO>> getMyCourses() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Teacher teacher = teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

        return ResponseEntity.ok(courseService.getCoursesByTeacherId(teacher.getId()));
    }

    @PostMapping("/courses")
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
        // If teacher is creating, automatically assign to them
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Teacher teacher = teacherRepository.findByUserId(user.getId())
                .orElse(null);

        if (teacher != null && courseDTO.getTeacherId() == null) {
            courseDTO.setTeacherId(teacher.getId());
        }

        return ResponseEntity.ok(courseService.createCourse(courseDTO));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @RequestBody CourseDTO courseDTO) {
        return ResponseEntity.ok(courseService.updateCourse(id, courseDTO));
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<MessageResponse> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(new MessageResponse("Course deleted successfully"));
    }

    // ================== VIEW DEPARTMENTS ==================

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }
}
