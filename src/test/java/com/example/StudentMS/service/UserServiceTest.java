package com.example.StudentMS.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
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

import com.example.StudentMS.dto.UserDTO;
import com.example.StudentMS.entity.Role;
import com.example.StudentMS.entity.User;
import com.example.StudentMS.repository.RoleRepository;
import com.example.StudentMS.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    private User createUser(Long id, String username) {
        return User.builder()
                .id(id)
                .username(username)
                .email(username + "@test.com")
                .password("password")
                .firstName("First")
                .lastName("Last")
                .enabled(true)
                .roles(Set.of(Role.builder().id(1L).name("ROLE_STUDENT").build()))
                .build();
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("should return all users")
        void shouldReturnAllUsers() {
            when(userRepository.findAll()).thenReturn(List.of(
                    createUser(1L, "user1"), createUser(2L, "user2")));

            List<UserDTO> result = userService.getAllUsers();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should return empty list when no users")
        void shouldReturnEmptyList() {
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            List<UserDTO> result = userService.getAllUsers();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("should return user when found")
        void shouldReturnUser() {
            User user = createUser(1L, "admin");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            UserDTO result = userService.getUserById(1L);

            assertThat(result.getUsername()).isEqualTo("admin");
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("should create user with default STUDENT role")
        void shouldCreateWithDefaultRole() {
            UserDTO dto = new UserDTO();
            dto.setUsername("newuser");
            dto.setEmail("new@test.com");
            dto.setPassword("password123");
            dto.setFirstName("New");
            dto.setLastName("User");

            Role studentRole = Role.builder().id(1L).name("ROLE_STUDENT").build();

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
            when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(10L);
                u.setRoles(Set.of(studentRole));
                return u;
            });

            UserDTO result = userService.createUser(dto);

            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should throw when username already exists")
        void shouldThrowWhenUsernameExists() {
            UserDTO dto = new UserDTO();
            dto.setUsername("existing");
            when(userRepository.existsByUsername("existing")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Username already exists");
        }

        @Test
        @DisplayName("should throw when email already exists")
        void shouldThrowWhenEmailExists() {
            UserDTO dto = new UserDTO();
            dto.setUsername("newuser");
            dto.setEmail("existing@test.com");
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Email already exists");
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("should delete user when found")
        void shouldDeleteUser() {
            when(userRepository.existsById(1L)).thenReturn(true);

            userService.deleteUser(1L);

            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when user not found for deletion")
        void shouldThrowWhenNotFoundForDelete() {
            when(userRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found");
        }
    }
}
