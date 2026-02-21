package com.example.StudentMS.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.StudentMS.entity.Role;
import com.example.StudentMS.entity.User;
import com.example.StudentMS.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("should load user by username successfully")
    void shouldLoadUserByUsername() {
        Role adminRole = Role.builder().id(1L).name("ROLE_ADMIN").build();
        User user = User.builder()
                .id(1L)
                .username("admin")
                .password("password123")
                .enabled(true)
                .roles(Set.of(adminRole))
                .build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getPassword()).isEqualTo("password123");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("should load user with multiple roles")
    void shouldLoadUserWithMultipleRoles() {
        Role adminRole = Role.builder().id(1L).name("ROLE_ADMIN").build();
        Role teacherRole = Role.builder().id(2L).name("ROLE_TEACHER").build();
        User user = User.builder()
                .id(1L)
                .username("superuser")
                .password("pass")
                .enabled(true)
                .roles(Set.of(adminRole, teacherRole))
                .build();

        when(userRepository.findByUsername("superuser")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("superuser");

        assertThat(userDetails.getAuthorities()).hasSize(2);
    }

    @Test
    @DisplayName("should throw UsernameNotFoundException when user not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with username: unknown");
    }

    @Test
    @DisplayName("should return disabled user correctly")
    void shouldReturnDisabledUser() {
        User user = User.builder()
                .id(2L)
                .username("disabled")
                .password("pass")
                .enabled(false)
                .roles(Set.of(Role.builder().id(1L).name("ROLE_STUDENT").build()))
                .build();

        when(userRepository.findByUsername("disabled")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("disabled");

        assertThat(userDetails.isEnabled()).isFalse();
    }
}
