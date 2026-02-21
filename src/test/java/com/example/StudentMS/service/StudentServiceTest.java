package com.example.StudentMS.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.StudentMS.dto.CreateStudentRequest;
import com.example.StudentMS.dto.StudentDTO;
import com.example.StudentMS.entity.Role;
import com.example.StudentMS.entity.Student;
import com.example.StudentMS.entity.User;
import com.example.StudentMS.repository.RoleRepository;
import com.example.StudentMS.repository.StudentRepository;
import com.example.StudentMS.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Unit Tests")
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    private Student student;
    private User user;
    private Role studentRole;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("student1")
                .email("student1@test.com")
                .password("encoded_pass")
                .firstName("Alice")
                .lastName("Brown")
                .enabled(true)
                .build();

        student = Student.builder()
                .id(1L)
                .user(user)
                .studentId("STU001")
                .department("CSE")
                .semester(3)
                .gpa(new BigDecimal("3.50"))
                .build();

        studentRole = Role.builder()
                .id(1L)
                .name("ROLE_STUDENT")
                .build();
    }

    @Nested
    @DisplayName("getAllStudents")
    class GetAllStudents {

        @Test
        @DisplayName("should return all students")
        void shouldReturnAllStudents() {
            User user2 = User.builder().id(2L).username("student2").email("s2@test.com")
                    .password("pass").firstName("Bob").lastName("Smith").build();
            Student student2 = Student.builder().id(2L).user(user2).studentId("STU002")
                    .department("EEE").build();

            when(studentRepository.findAll()).thenReturn(Arrays.asList(student, student2));

            List<StudentDTO> result = studentService.getAllStudents();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getStudentId()).isEqualTo("STU001");
            assertThat(result.get(0).getFirstName()).isEqualTo("Alice");
            assertThat(result.get(1).getStudentId()).isEqualTo("STU002");
        }

        @Test
        @DisplayName("should return empty list when no students")
        void shouldReturnEmptyList() {
            when(studentRepository.findAll()).thenReturn(List.of());
            assertThat(studentService.getAllStudents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getStudentById")
    class GetStudentById {

        @Test
        @DisplayName("should return student when found")
        void shouldReturnStudentWhenFound() {
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

            StudentDTO result = studentService.getStudentById(1L);

            assertThat(result.getStudentId()).isEqualTo("STU001");
            assertThat(result.getUsername()).isEqualTo("student1");
            assertThat(result.getDepartment()).isEqualTo("CSE");
            assertThat(result.getGpa()).isEqualTo(new BigDecimal("3.50"));
        }

        @Test
        @DisplayName("should throw when student not found")
        void shouldThrowWhenNotFound() {
            when(studentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.getStudentById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Student not found");
        }
    }

    @Nested
    @DisplayName("getStudentsByDepartment")
    class GetStudentsByDepartment {

        @Test
        @DisplayName("should return students by department")
        void shouldReturnByDepartment() {
            when(studentRepository.findByDepartment("CSE")).thenReturn(List.of(student));

            List<StudentDTO> result = studentService.getStudentsByDepartment("CSE");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDepartment()).isEqualTo("CSE");
        }
    }

    @Nested
    @DisplayName("createStudent")
    class CreateStudent {

        @Test
        @DisplayName("should create student with user account")
        void shouldCreateStudent() {
            CreateStudentRequest request = CreateStudentRequest.builder()
                    .username("newstudent").email("new@test.com").password("pass123")
                    .firstName("New").lastName("Student")
                    .studentId("STU003").department("CSE").semester(1)
                    .gpa(new BigDecimal("0.00")).build();

            when(userRepository.existsByUsername("newstudent")).thenReturn(false);
            when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
            when(studentRepository.existsByStudentId("STU003")).thenReturn(false);
            when(passwordEncoder.encode("pass123")).thenReturn("encoded");
            when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(10L);
                return u;
            });
            when(studentRepository.save(any(Student.class))).thenAnswer(inv -> {
                Student s = inv.getArgument(0);
                s.setId(10L);
                return s;
            });

            StudentDTO result = studentService.createStudent(request);

            assertThat(result.getStudentId()).isEqualTo("STU003");
            assertThat(result.getFirstName()).isEqualTo("New");
            verify(userRepository).save(any(User.class));
            verify(studentRepository).save(any(Student.class));
        }

        @Test
        @DisplayName("should throw when username already taken")
        void shouldThrowWhenUsernameTaken() {
            CreateStudentRequest request = CreateStudentRequest.builder()
                    .username("student1").email("new@test.com").password("pass")
                    .studentId("STU003").build();

            when(userRepository.existsByUsername("student1")).thenReturn(true);

            assertThatThrownBy(() -> studentService.createStudent(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Username is already taken!");
        }

        @Test
        @DisplayName("should throw when email already in use")
        void shouldThrowWhenEmailInUse() {
            CreateStudentRequest request = CreateStudentRequest.builder()
                    .username("newuser").email("student1@test.com").password("pass")
                    .studentId("STU003").build();

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("student1@test.com")).thenReturn(true);

            assertThatThrownBy(() -> studentService.createStudent(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Email is already in use!");
        }

        @Test
        @DisplayName("should throw when student ID already exists")
        void shouldThrowWhenStudentIdExists() {
            CreateStudentRequest request = CreateStudentRequest.builder()
                    .username("newuser").email("new@test.com").password("pass")
                    .studentId("STU001").build();

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
            when(studentRepository.existsByStudentId("STU001")).thenReturn(true);

            assertThatThrownBy(() -> studentService.createStudent(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Student ID already exists!");
        }
    }

    @Nested
    @DisplayName("updateStudent")
    class UpdateStudent {

        @Test
        @DisplayName("should update student successfully")
        void shouldUpdateStudent() {
            StudentDTO dto = StudentDTO.builder()
                    .firstName("UpdatedAlice").lastName("UpdatedBrown")
                    .department("EEE").semester(4).gpa(new BigDecimal("3.80")).build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(studentRepository.save(any(Student.class))).thenReturn(student);

            StudentDTO result = studentService.updateStudent(1L, dto);

            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
            verify(studentRepository).save(any(Student.class));
        }
    }

    @Nested
    @DisplayName("deleteStudent")
    class DeleteStudent {

        @Test
        @DisplayName("should delete student and user account")
        void shouldDeleteStudent() {
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

            studentService.deleteStudent(1L);

            verify(studentRepository).deleteById(1L);
            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when student not found")
        void shouldThrowWhenNotFound() {
            when(studentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.deleteStudent(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Student not found");
        }
    }
}
