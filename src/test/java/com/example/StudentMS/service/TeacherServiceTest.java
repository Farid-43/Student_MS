package com.example.StudentMS.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.StudentMS.dto.CreateTeacherRequest;
import com.example.StudentMS.dto.TeacherDTO;
import com.example.StudentMS.entity.Role;
import com.example.StudentMS.entity.Teacher;
import com.example.StudentMS.entity.User;
import com.example.StudentMS.repository.RoleRepository;
import com.example.StudentMS.repository.TeacherRepository;
import com.example.StudentMS.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherService Unit Tests")
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TeacherService teacherService;

    private User createUser(Long id, String username) {
        return User.builder()
                .id(id)
                .username(username)
                .email(username + "@test.com")
                .password("password")
                .firstName("First")
                .lastName("Last")
                .enabled(true)
                .build();
    }

    private Teacher createTeacher(Long id, User user, String employeeId) {
        return Teacher.builder()
                .id(id)
                .user(user)
                .employeeId(employeeId)
                .department("Computer Science")
                .designation("Professor")
                .joiningDate(LocalDate.of(2023, 1, 15))
                .build();
    }

    @Nested
    @DisplayName("getAllTeachers")
    class GetAllTeachers {

        @Test
        @DisplayName("should return all teachers")
        void shouldReturnAllTeachers() {
            User user1 = createUser(1L, "teacher1");
            User user2 = createUser(2L, "teacher2");
            List<Teacher> teachers = List.of(
                    createTeacher(1L, user1, "EMP001"),
                    createTeacher(2L, user2, "EMP002"));
            when(teacherRepository.findAll()).thenReturn(teachers);

            List<TeacherDTO> result = teacherService.getAllTeachers();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getEmployeeId()).isEqualTo("EMP001");
        }

        @Test
        @DisplayName("should return empty list when no teachers")
        void shouldReturnEmptyList() {
            when(teacherRepository.findAll()).thenReturn(Collections.emptyList());

            List<TeacherDTO> result = teacherService.getAllTeachers();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTeacherById")
    class GetTeacherById {

        @Test
        @DisplayName("should return teacher when found")
        void shouldReturnTeacher() {
            User user = createUser(1L, "teacher1");
            Teacher teacher = createTeacher(1L, user, "EMP001");
            when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

            TeacherDTO result = teacherService.getTeacherById(1L);

            assertThat(result.getEmployeeId()).isEqualTo("EMP001");
            assertThat(result.getUsername()).isEqualTo("teacher1");
        }

        @Test
        @DisplayName("should throw when teacher not found")
        void shouldThrowWhenNotFound() {
            when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> teacherService.getTeacherById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Teacher not found");
        }
    }

    @Nested
    @DisplayName("createTeacher")
    class CreateTeacher {

        @Test
        @DisplayName("should create teacher successfully")
        void shouldCreateTeacher() {
            CreateTeacherRequest request = new CreateTeacherRequest();
            request.setUsername("newteacher");
            request.setEmail("new@test.com");
            request.setPassword("password");
            request.setFirstName("New");
            request.setLastName("Teacher");
            request.setEmployeeId("EMP003");
            request.setDepartment("Physics");
            request.setDesignation("Lecturer");
            request.setJoiningDate(LocalDate.now());

            Role teacherRole = Role.builder().id(2L).name("ROLE_TEACHER").build();

            when(userRepository.existsByUsername("newteacher")).thenReturn(false);
            when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
            when(teacherRepository.existsByEmployeeId("EMP003")).thenReturn(false);
            when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
            when(roleRepository.findByName("ROLE_TEACHER")).thenReturn(Optional.of(teacherRole));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(10L);
                return u;
            });
            when(teacherRepository.save(any(Teacher.class))).thenAnswer(inv -> {
                Teacher t = inv.getArgument(0);
                t.setId(5L);
                return t;
            });

            TeacherDTO result = teacherService.createTeacher(request);

            assertThat(result).isNotNull();
            assertThat(result.getEmployeeId()).isEqualTo("EMP003");
            verify(userRepository).save(any(User.class));
            verify(teacherRepository).save(any(Teacher.class));
        }

        @Test
        @DisplayName("should throw when username already taken")
        void shouldThrowWhenUsernameTaken() {
            CreateTeacherRequest request = new CreateTeacherRequest();
            request.setUsername("existing");
            when(userRepository.existsByUsername("existing")).thenReturn(true);

            assertThatThrownBy(() -> teacherService.createTeacher(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Username is already taken!");
        }

        @Test
        @DisplayName("should throw when employee ID exists")
        void shouldThrowWhenEmployeeIdExists() {
            CreateTeacherRequest request = new CreateTeacherRequest();
            request.setUsername("newuser");
            request.setEmail("new@test.com");
            request.setEmployeeId("EMP001");
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
            when(teacherRepository.existsByEmployeeId("EMP001")).thenReturn(true);

            assertThatThrownBy(() -> teacherService.createTeacher(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Employee ID already exists!");
        }
    }

    @Nested
    @DisplayName("deleteTeacher")
    class DeleteTeacher {

        @Test
        @DisplayName("should delete teacher and user")
        void shouldDeleteTeacher() {
            User user = createUser(1L, "teacher1");
            Teacher teacher = createTeacher(1L, user, "EMP001");
            when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

            teacherService.deleteTeacher(1L);

            verify(teacherRepository).deleteById(1L);
            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when teacher not found for deletion")
        void shouldThrowWhenNotFoundForDelete() {
            when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> teacherService.deleteTeacher(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Teacher not found");
        }
    }
}
