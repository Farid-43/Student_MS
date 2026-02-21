package com.example.StudentMS.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.StudentMS.dto.JwtResponse;
import com.example.StudentMS.dto.LoginRequest;
import com.example.StudentMS.dto.MessageResponse;
import com.example.StudentMS.dto.RegisterRequest;
import com.example.StudentMS.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @Nested
    @DisplayName("Login Endpoint")
    class LoginEndpoint {

        @Test
        @DisplayName("POST /api/auth/login - should return JWT token on valid login")
        void shouldReturnJwtOnValidLogin() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .username("admin").password("admin123").build();

            JwtResponse response = JwtResponse.builder()
                    .token("jwt-token-123")
                    .username("admin")
                    .email("admin@example.com")
                    .roles(List.of("ROLE_ADMIN"))
                    .build();

            when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token-123"))
                    .andExpect(jsonPath("$.username").value("admin"));
        }

        @Test
        @DisplayName("POST /api/auth/login - should return 400 on invalid credentials")
        void shouldReturnBadRequestOnInvalidCredentials() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .username("admin").password("wrong").build();

            when(authService.authenticateUser(any(LoginRequest.class)))
                    .thenThrow(new RuntimeException("Bad credentials"));

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Register Endpoint")
    class RegisterEndpoint {

        @Test
        @DisplayName("POST /api/auth/register - should register a user successfully")
        void shouldRegisterUser() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("new@example.com");
            request.setPassword("password123");
            request.setFirstName("New");
            request.setLastName("User");

            MessageResponse response = new MessageResponse("User registered successfully!");
            when(authService.registerUser(any(RegisterRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User registered successfully!"));
        }
    }
}
