package com.example.StudentMS.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.StudentMS.dto.CreateStudentRequest;
import com.example.StudentMS.dto.StudentDTO;
import com.example.StudentMS.entity.Role;
import com.example.StudentMS.entity.Student;
import com.example.StudentMS.entity.User;
import com.example.StudentMS.repository.RoleRepository;
import com.example.StudentMS.repository.StudentRepository;
import com.example.StudentMS.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<StudentDTO> getStudentsByDepartment(String department) {
        return studentRepository.findByDepartment(department).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return mapToDTO(student);
    }

    public StudentDTO getStudentByStudentId(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return mapToDTO(student);
    }

    @Transactional
    public StudentDTO createStudent(CreateStudentRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }
        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new RuntimeException("Student ID already exists!");
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .build();

        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Create student profile
        Student student = Student.builder()
                .user(savedUser)
                .studentId(request.getStudentId())
                .department(request.getDepartment())
                .semester(request.getSemester())
                .enrollmentDate(request.getEnrollmentDate())
                .gpa(request.getGpa())
                .build();

        Student savedStudent = studentRepository.save(student);
        return mapToDTO(savedStudent);
    }

    @Transactional
    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        student.setDepartment(studentDTO.getDepartment());
        student.setSemester(studentDTO.getSemester());
        student.setGpa(studentDTO.getGpa());

        User user = student.getUser();
        user.setFirstName(studentDTO.getFirstName());
        user.setLastName(studentDTO.getLastName());
        userRepository.save(user);

        Student savedStudent = studentRepository.save(student);
        return mapToDTO(savedStudent);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Long userId = student.getUser().getId();
        studentRepository.deleteById(id);
        userRepository.deleteById(userId);
    }

    private StudentDTO mapToDTO(Student student) {
        User user = student.getUser();
        return StudentDTO.builder()
                .id(student.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .studentId(student.getStudentId())
                .department(student.getDepartment())
                .semester(student.getSemester())
                .enrollmentDate(student.getEnrollmentDate())
                .gpa(student.getGpa())
                .build();
    }
}
