package com.example.StudentMS.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.StudentMS.dto.JwtResponse;
import com.example.StudentMS.dto.LoginRequest;
import com.example.StudentMS.dto.MessageResponse;
import com.example.StudentMS.dto.RegisterRequest;
import com.example.StudentMS.entity.Role;
import com.example.StudentMS.entity.User;
import com.example.StudentMS.repository.RoleRepository;
import com.example.StudentMS.repository.UserRepository;
import com.example.StudentMS.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("authenticateUser")
    class AuthenticateUser {

        @Test
        @DisplayName("should authenticate and return JWT response")
        void shouldAuthenticateSuccessfully() {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("admin");
            loginRequest.setPassword("password");

            org.springframework.security.core.userdetails.User principal = new org.springframework.security.core.userdetails.User(
                    "admin", "password",
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities());

            User user = User.builder()
                    .id(1L)
                    .username("admin")
                    .email("admin@test.com")
                    .build();

            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token-123");
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

            JwtResponse result = authService.authenticateUser(loginRequest);

            assertThat(result.getToken()).isEqualTo("jwt-token-123");
            assertThat(result.getUsername()).isEqualTo("admin");
            assertThat(result.getEmail()).isEqualTo("admin@test.com");
            assertThat(result.getRoles()).contains("ROLE_ADMIN");
        }

        @Test
        @DisplayName("should throw on invalid credentials")
        void shouldThrowOnInvalidCredentials() {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("admin");
            loginRequest.setPassword("wrong");

            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.authenticateUser(loginRequest))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("registerUser")
    class RegisterUser {

        @Test
        @DisplayName("should register user successfully with default role")
        void shouldRegisterWithDefaultRole() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("new@test.com");
            request.setPassword("password");
            request.setFirstName("New");
            request.setLastName("User");
            request.setRoles(null);

            Role studentRole = Role.builder().id(1L).name("ROLE_STUDENT").build();

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
            when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
            when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));

            MessageResponse result = authService.registerUser(request);

            assertThat(result.getMessage()).isEqualTo("User registered successfully!");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should register user with specified roles")
        void shouldRegisterWithSpecifiedRoles() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("teacher1");
            request.setEmail("teacher@test.com");
            request.setPassword("password");
            request.setFirstName("Teacher");
            request.setLastName("One");
            request.setRoles(Set.of("ROLE_TEACHER"));

            Role teacherRole = Role.builder().id(2L).name("ROLE_TEACHER").build();

            when(userRepository.existsByUsername("teacher1")).thenReturn(false);
            when(userRepository.existsByEmail("teacher@test.com")).thenReturn(false);
            when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
            when(roleRepository.findByName("ROLE_TEACHER")).thenReturn(Optional.of(teacherRole));

            MessageResponse result = authService.registerUser(request);

            assertThat(result.getMessage()).isEqualTo("User registered successfully!");
        }

        @Test
        @DisplayName("should throw when username is taken")
        void shouldThrowWhenUsernameTaken() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("existing");
            when(userRepository.existsByUsername("existing")).thenReturn(true);

            assertThatThrownBy(() -> authService.registerUser(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Username is already taken!");
        }

        @Test
        @DisplayName("should throw when email is taken")
        void shouldThrowWhenEmailTaken() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("existing@test.com");
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.registerUser(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Email is already in use!");
        }
    }
}
