package com.example.StudentMS.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.StudentMS.dto.CourseDTO;
import com.example.StudentMS.dto.CreateStudentRequest;
import com.example.StudentMS.dto.DepartmentDTO;
import com.example.StudentMS.dto.StudentDTO;
import com.example.StudentMS.security.CustomUserDetailsService;
import com.example.StudentMS.security.JwtTokenProvider;
import com.example.StudentMS.service.CourseService;
import com.example.StudentMS.service.DepartmentService;
import com.example.StudentMS.service.StudentService;
import com.example.StudentMS.service.TeacherService;
import com.example.StudentMS.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AdminController Unit Tests")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private StudentService studentService;
    @MockitoBean
    private TeacherService teacherService;
    @MockitoBean
    private CourseService courseService;
    @MockitoBean
    private DepartmentService departmentService;

    private String adminToken;
    private String teacherToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        UserDetails adminUser = User.withUsername("admin")
                .password("password")
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                .build();
        UserDetails teacherUser = User.withUsername("teacher")
                .password("password")
                .authorities(new SimpleGrantedAuthority("ROLE_TEACHER"))
                .build();
        UserDetails studentUser = User.withUsername("student")
                .password("password")
                .authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))
                .build();

        when(customUserDetailsService.loadUserByUsername("admin")).thenReturn(adminUser);
        when(customUserDetailsService.loadUserByUsername("teacher")).thenReturn(teacherUser);
        when(customUserDetailsService.loadUserByUsername("student")).thenReturn(studentUser);

        adminToken = jwtTokenProvider.generateToken(
                new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities()));
        teacherToken = jwtTokenProvider.generateToken(
                new UsernamePasswordAuthenticationToken(teacherUser, null, teacherUser.getAuthorities()));
        studentToken = jwtTokenProvider.generateToken(
                new UsernamePasswordAuthenticationToken(studentUser, null, studentUser.getAuthorities()));
    }

    // ================== STUDENT ENDPOINTS ==================

    @Nested
    @DisplayName("Student Endpoints")
    class StudentEndpoints {

        @Test
        @DisplayName("GET /api/admin/students - should return all students")
        void shouldReturnAllStudents() throws Exception {
            StudentDTO student1 = StudentDTO.builder()
                    .id(1L).studentId("STU001").firstName("John").lastName("Doe")
                    .department("CS").semester(3).gpa(BigDecimal.valueOf(3.5)).build();
            StudentDTO student2 = StudentDTO.builder()
                    .id(2L).studentId("STU002").firstName("Jane").lastName("Smith")
                    .department("EE").semester(2).gpa(BigDecimal.valueOf(3.8)).build();

            when(studentService.getAllStudents()).thenReturn(Arrays.asList(student1, student2));

            mockMvc.perform(get("/api/admin/students")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].studentId").value("STU001"))
                    .andExpect(jsonPath("$[1].studentId").value("STU002"));
        }

        @Test
        @DisplayName("GET /api/admin/students/{id} - should return student by id")
        void shouldReturnStudentById() throws Exception {
            StudentDTO student = StudentDTO.builder()
                    .id(1L).studentId("STU001").firstName("John").lastName("Doe")
                    .department("CS").semester(3).gpa(BigDecimal.valueOf(3.5)).build();

            when(studentService.getStudentById(1L)).thenReturn(student);

            mockMvc.perform(get("/api/admin/students/1")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.studentId").value("STU001"))
                    .andExpect(jsonPath("$.firstName").value("John"));
        }

        @Test
        @DisplayName("POST /api/admin/students - should create a student")
        void shouldCreateStudent() throws Exception {
            CreateStudentRequest request = new CreateStudentRequest();
            request.setUsername("newstudent");
            request.setEmail("new@test.com");
            request.setPassword("password");
            request.setFirstName("New");
            request.setLastName("Student");
            request.setStudentId("STU003");
            request.setDepartment("CS");
            request.setSemester(1);

            StudentDTO created = StudentDTO.builder()
                    .id(3L).studentId("STU003").firstName("New").lastName("Student")
                    .department("CS").semester(1).build();

            when(studentService.createStudent(any())).thenReturn(created);

            mockMvc.perform(post("/api/admin/students")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.studentId").value("STU003"));
        }

        @Test
        @DisplayName("DELETE /api/admin/students/{id} - should delete a student")
        void shouldDeleteStudent() throws Exception {
            doNothing().when(studentService).deleteStudent(1L);

            mockMvc.perform(delete("/api/admin/students/1")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            verify(studentService).deleteStudent(1L);
        }

    }

    // ================== COURSE ENDPOINTS ==================

    @Nested
    @DisplayName("Course Endpoints")
    class CourseEndpoints {

        @Test
        @DisplayName("GET /api/admin/courses - should return all courses")
        void shouldReturnAllCourses() throws Exception {
            CourseDTO course1 = CourseDTO.builder()
                    .id(1L).courseCode("CS101").courseName("Intro to CS").credits(3).build();
            CourseDTO course2 = CourseDTO.builder()
                    .id(2L).courseCode("CS201").courseName("Data Structures").credits(4).build();

            when(courseService.getAllCourses()).thenReturn(Arrays.asList(course1, course2));

            mockMvc.perform(get("/api/admin/courses")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("POST /api/admin/courses - should create a course")
        void shouldCreateCourse() throws Exception {
            CourseDTO input = CourseDTO.builder()
                    .courseCode("CS301").courseName("Algorithms").credits(3).build();

            CourseDTO created = CourseDTO.builder()
                    .id(3L).courseCode("CS301").courseName("Algorithms").credits(3).build();

            when(courseService.createCourse(any())).thenReturn(created);

            mockMvc.perform(post("/api/admin/courses")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.courseCode").value("CS301"));
        }

        @Test
        @DisplayName("DELETE /api/admin/courses/{id} - should delete a course")
        void shouldDeleteCourse() throws Exception {
            doNothing().when(courseService).deleteCourse(1L);

            mockMvc.perform(delete("/api/admin/courses/1")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            verify(courseService).deleteCourse(1L);
        }
    }

    // ================== DEPARTMENT ENDPOINTS ==================

    @Nested
    @DisplayName("Department Endpoints")
    class DepartmentEndpoints {

        @Test
        @DisplayName("GET /api/admin/departments - should return all departments")
        void shouldReturnAllDepartments() throws Exception {
            DepartmentDTO dept = DepartmentDTO.builder()
                    .id(1L).deptCode("CS").deptName("Computer Science").build();

            when(departmentService.getAllDepartments()).thenReturn(List.of(dept));

            mockMvc.perform(get("/api/admin/departments")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @DisplayName("POST /api/admin/departments - should create a department")
        void shouldCreateDepartment() throws Exception {
            DepartmentDTO input = DepartmentDTO.builder()
                    .deptCode("EE").deptName("Electrical Engineering")
                    .description("EE Dept").headOfDept("Dr. Volt").build();

            DepartmentDTO created = DepartmentDTO.builder()
                    .id(2L).deptCode("EE").deptName("Electrical Engineering")
                    .description("EE Dept").headOfDept("Dr. Volt").build();

            when(departmentService.createDepartment(any())).thenReturn(created);

            mockMvc.perform(post("/api/admin/departments")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.deptCode").value("EE"));
        }
    }

    // ================== AUTHORIZATION TESTS ==================

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/admin/students"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 for TEACHER role on admin endpoints")
        void shouldReturn403ForTeacher() throws Exception {
            mockMvc.perform(get("/api/admin/students")
                    .header("Authorization", "Bearer " + teacherToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 403 for STUDENT role on admin endpoints")
        void shouldReturn403ForStudent() throws Exception {
            mockMvc.perform(get("/api/admin/students")
                    .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden());
        }
    }
}
