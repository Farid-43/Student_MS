package com.example.StudentMS.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.StudentMS.dto.CourseDTO;
import com.example.StudentMS.dto.CreateStudentRequest;
import com.example.StudentMS.dto.CreateTeacherRequest;
import com.example.StudentMS.dto.DepartmentDTO;
import com.example.StudentMS.entity.Role;
import com.example.StudentMS.entity.User;
import com.example.StudentMS.repository.RoleRepository;
import com.example.StudentMS.repository.UserRepository;
import com.example.StudentMS.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for Admin endpoints.
 * Tests full request flow with JWT authentication and database operations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Admin API Integration Tests")
class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private String teacherToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        // Ensure roles exist
        ensureRoleExists("ROLE_ADMIN", "Admin role");
        ensureRoleExists("ROLE_TEACHER", "Teacher role");
        ensureRoleExists("ROLE_STUDENT", "Student role");

        // Create admin user and get token
        adminToken = getTokenForUser("adminuser", "ROLE_ADMIN");
        teacherToken = getTokenForUser("teacheruser", "ROLE_TEACHER");
        studentToken = getTokenForUser("studentuser", "ROLE_STUDENT");
    }

    // ==================== Department Tests ====================

    @Test
    @Order(1)
    @DisplayName("Admin should create a department")
    void adminShouldCreateDepartment() throws Exception {
        DepartmentDTO dept = new DepartmentDTO();
        dept.setDeptCode("CS");
        dept.setDeptName("Computer Science");
        dept.setDescription("CS Department");
        dept.setHeadOfDept("Dr. Smith");

        mockMvc.perform(post("/api/admin/departments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dept)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deptCode").value("CS"))
                .andExpect(jsonPath("$.deptName").value("Computer Science"));
    }

    @Test
    @Order(2)
    @DisplayName("Admin should get all departments")
    void adminShouldGetAllDepartments() throws Exception {
        mockMvc.perform(get("/api/admin/departments")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ==================== Course Tests ====================

    @Test
    @Order(3)
    @DisplayName("Admin should create a course")
    void adminShouldCreateCourse() throws Exception {
        CourseDTO course = new CourseDTO();
        course.setCourseCode("CS101");
        course.setCourseName("Intro to CS");
        course.setDescription("Introductory CS course");
        course.setCredits(3);

        mockMvc.perform(post("/api/admin/courses")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(course)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseCode").value("CS101"))
                .andExpect(jsonPath("$.courseName").value("Intro to CS"));
    }

    @Test
    @Order(4)
    @DisplayName("Admin should get all courses")
    void adminShouldGetAllCourses() throws Exception {
        mockMvc.perform(get("/api/admin/courses")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ==================== Student Tests ====================

    @Test
    @Order(5)
    @DisplayName("Admin should create a student")
    void adminShouldCreateStudent() throws Exception {
        CreateStudentRequest request = new CreateStudentRequest();
        request.setUsername("newstudent1");
        request.setEmail("newstudent1@test.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("Student");
        request.setStudentId("STU001");
        request.setDepartment("Computer Science");
        request.setSemester(3);

        mockMvc.perform(post("/api/admin/students")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value("STU001"))
                .andExpect(jsonPath("$.firstName").value("New"));
    }

    @Test
    @Order(6)
    @DisplayName("Admin should get all students")
    void adminShouldGetAllStudents() throws Exception {
        mockMvc.perform(get("/api/admin/students")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ==================== Teacher Tests ====================

    @Test
    @Order(7)
    @DisplayName("Admin should create a teacher")
    void adminShouldCreateTeacher() throws Exception {
        CreateTeacherRequest request = new CreateTeacherRequest();
        request.setUsername("newteacher1");
        request.setEmail("newteacher1@test.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("Teacher");
        request.setEmployeeId("EMP001");
        request.setDepartment("Computer Science");
        request.setDesignation("Professor");

        mockMvc.perform(post("/api/admin/teachers")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value("EMP001"))
                .andExpect(jsonPath("$.firstName").value("New"));
    }

    @Test
    @Order(8)
    @DisplayName("Admin should get all teachers")
    void adminShouldGetAllTeachers() throws Exception {
        mockMvc.perform(get("/api/admin/teachers")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ==================== Authorization Tests ====================

    @Test
    @Order(20)
    @DisplayName("Unauthenticated user should be rejected from admin endpoints")
    void unauthenticatedShouldBeRejected() throws Exception {
        mockMvc.perform(get("/api/admin/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(21)
    @DisplayName("Teacher should be forbidden from admin endpoints")
    void teacherShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/students")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(22)
    @DisplayName("Student should be forbidden from admin endpoints")
    void studentShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/students")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    // ==================== Helper Methods ====================

    private void ensureRoleExists(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(Role.builder().name(name).description(description).build());
        }
    }

    private String getTokenForUser(String username, String roleName) {
        if (userRepository.findByUsername(username).isEmpty()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException(roleName + " not found"));
            Set<Role> roles = new HashSet<>();
            roles.add(role);

            User user = User.builder()
                    .username(username)
                    .email(username + "@test.com")
                    .password("password123")
                    .firstName("Test")
                    .lastName("User")
                    .enabled(true)
                    .roles(roles)
                    .build();
            userRepository.save(user);
        }

        // Generate JWT token
        org.springframework.security.core.userdetails.User principal = new org.springframework.security.core.userdetails.User(
                username, "password123",
                List.of(new SimpleGrantedAuthority(roleName)));

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null,
                principal.getAuthorities());

        return jwtTokenProvider.generateToken(auth);
    }
}
