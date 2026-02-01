package com.example.StudentMS.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.StudentMS.dto.CreateTeacherRequest;
import com.example.StudentMS.dto.TeacherDTO;
import com.example.StudentMS.entity.Role;
import com.example.StudentMS.entity.Teacher;
import com.example.StudentMS.entity.User;
import com.example.StudentMS.repository.RoleRepository;
import com.example.StudentMS.repository.TeacherRepository;
import com.example.StudentMS.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<TeacherDTO> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TeacherDTO getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return mapToDTO(teacher);
    }

    public TeacherDTO getTeacherByEmployeeId(String employeeId) {
        Teacher teacher = teacherRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return mapToDTO(teacher);
    }

    @Transactional
    public TeacherDTO createTeacher(CreateTeacherRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }
        if (teacherRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new RuntimeException("Employee ID already exists!");
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

        Role teacherRole = roleRepository.findByName("ROLE_TEACHER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(teacherRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Create teacher profile
        Teacher teacher = Teacher.builder()
                .user(savedUser)
                .employeeId(request.getEmployeeId())
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .joiningDate(request.getJoiningDate())
                .build();

        Teacher savedTeacher = teacherRepository.save(teacher);
        return mapToDTO(savedTeacher);
    }

    @Transactional
    public TeacherDTO updateTeacher(Long id, TeacherDTO teacherDTO) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        teacher.setDepartment(teacherDTO.getDepartment());
        teacher.setDesignation(teacherDTO.getDesignation());

        User user = teacher.getUser();
        user.setFirstName(teacherDTO.getFirstName());
        user.setLastName(teacherDTO.getLastName());
        userRepository.save(user);

        Teacher savedTeacher = teacherRepository.save(teacher);
        return mapToDTO(savedTeacher);
    }

    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Long userId = teacher.getUser().getId();
        teacherRepository.deleteById(id);
        userRepository.deleteById(userId);
    }

    private TeacherDTO mapToDTO(Teacher teacher) {
        User user = teacher.getUser();
        return TeacherDTO.builder()
                .id(teacher.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .employeeId(teacher.getEmployeeId())
                .teacherId(teacher.getEmployeeId()) // alias for frontend
                .department(teacher.getDepartment())
                .designation(teacher.getDesignation())
                .joiningDate(teacher.getJoiningDate())
                .build();
    }
}
