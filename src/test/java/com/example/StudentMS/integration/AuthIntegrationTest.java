package com.example.StudentMS.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashSet;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.StudentMS.dto.JwtResponse;
import com.example.StudentMS.dto.LoginRequest;
import com.example.StudentMS.dto.RegisterRequest;
import com.example.StudentMS.entity.Role;
import com.example.StudentMS.entity.User;
import com.example.StudentMS.repository.RoleRepository;
import com.example.StudentMS.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for the Authentication flow.
 * Uses H2 in-memory database with full Spring context.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Authentication Integration Tests")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Ensure roles exist
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_ADMIN").description("Admin role").build());
        }
        if (roleRepository.findByName("ROLE_TEACHER").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_TEACHER").description("Teacher role").build());
        }
        if (roleRepository.findByName("ROLE_STUDENT").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_STUDENT").description("Student role").build());
        }
    }

    @Test
    @Order(1)
    @DisplayName("should register a new user successfully")
    void shouldRegisterUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("integrationuser");
        request.setEmail("integration@test.com");
        request.setPassword("password123");
        request.setFirstName("Integration");
        request.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        // Verify user exists in database
        assertThat(userRepository.findByUsername("integrationuser")).isPresent();
    }

    @Test
    @Order(2)
    @DisplayName("should reject duplicate username registration")
    void shouldRejectDuplicateUsername() throws Exception {
        // First, ensure a user exists
        ensureUserExists("duplicateuser", "dup@test.com");

        RegisterRequest request = new RegisterRequest();
        request.setUsername("duplicateuser");
        request.setEmail("other@test.com");
        request.setPassword("password123");
        request.setFirstName("Dup");
        request.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    @DisplayName("should login and receive JWT token")
    void shouldLoginAndReceiveToken() throws Exception {
        // Ensure user exists
        ensureUserExists("loginuser", "login@test.com");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginuser");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("loginuser"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JwtResponse jwtResponse = objectMapper.readValue(responseBody, JwtResponse.class);
        assertThat(jwtResponse.getToken()).isNotBlank();
    }

    @Test
    @Order(4)
    @DisplayName("should reject login with wrong password")
    void shouldRejectWrongPassword() throws Exception {
        ensureUserExists("wrongpassuser", "wrongpass@test.com");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wrongpassuser");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    @DisplayName("should reject login for non-existent user")
    void shouldRejectNonExistentUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    private void ensureUserExists(String username, String email) {
        if (userRepository.findByUsername(username).isEmpty()) {
            Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                    .orElseThrow(() -> new RuntimeException("ROLE_STUDENT not found"));
            Set<Role> roles = new HashSet<>();
            roles.add(studentRole);

            User user = User.builder()
                    .username(username)
                    .email(email)
                    .password("password123") // NoOpPasswordEncoder, plain text
                    .firstName("Test")
                    .lastName("User")
                    .enabled(true)
                    .roles(roles)
                    .build();
            userRepository.save(user);
        }
    }
}
