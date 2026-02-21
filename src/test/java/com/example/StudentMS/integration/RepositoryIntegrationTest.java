package com.example.StudentMS.integration;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.StudentMS.entity.Department;
import com.example.StudentMS.entity.Role;
import com.example.StudentMS.entity.User;
import com.example.StudentMS.repository.DepartmentRepository;
import com.example.StudentMS.repository.RoleRepository;
import com.example.StudentMS.repository.UserRepository;

/**
 * Integration tests for JPA repositories.
 * Tests actual database operations with H2.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Repository Integration Tests")
class RepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Nested
    @DisplayName("UserRepository")
    class UserRepositoryTests {

        @Test
        @DisplayName("should save and find user by username")
        void shouldFindByUsername() {
            User user = User.builder()
                    .username("repouser")
                    .email("repo@test.com")
                    .password("password")
                    .firstName("Repo")
                    .lastName("User")
                    .enabled(true)
                    .build();

            userRepository.save(user);

            Optional<User> found = userRepository.findByUsername("repouser");
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("repo@test.com");
        }

        @Test
        @DisplayName("should return empty when user not found by username")
        void shouldReturnEmptyForMissingUsername() {
            Optional<User> found = userRepository.findByUsername("nonexistent");
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("should check if username exists")
        void shouldCheckUsernameExists() {
            User user = User.builder()
                    .username("existsuser")
                    .email("exists@test.com")
                    .password("password")
                    .firstName("Exists")
                    .lastName("User")
                    .enabled(true)
                    .build();
            userRepository.save(user);

            assertThat(userRepository.existsByUsername("existsuser")).isTrue();
            assertThat(userRepository.existsByUsername("nope")).isFalse();
        }

        @Test
        @DisplayName("should check if email exists")
        void shouldCheckEmailExists() {
            User user = User.builder()
                    .username("emailuser")
                    .email("unique@test.com")
                    .password("password")
                    .firstName("Email")
                    .lastName("User")
                    .enabled(true)
                    .build();
            userRepository.save(user);

            assertThat(userRepository.existsByEmail("unique@test.com")).isTrue();
            assertThat(userRepository.existsByEmail("other@test.com")).isFalse();
        }

        @Test
        @DisplayName("should find user by email")
        void shouldFindByEmail() {
            User user = User.builder()
                    .username("findbyemail")
                    .email("findme@test.com")
                    .password("password")
                    .firstName("Find")
                    .lastName("Me")
                    .enabled(true)
                    .build();
            userRepository.save(user);

            Optional<User> found = userRepository.findByEmail("findme@test.com");
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("findbyemail");
        }
    }

    @Nested
    @DisplayName("RoleRepository")
    class RoleRepositoryTests {

        @Test
        @DisplayName("should save and find role by name")
        void shouldFindByName() {
            Role role = Role.builder()
                    .name("ROLE_TESTER")
                    .description("Test role")
                    .build();
            roleRepository.save(role);

            Optional<Role> found = roleRepository.findByName("ROLE_TESTER");
            assertThat(found).isPresent();
            assertThat(found.get().getDescription()).isEqualTo("Test role");
        }

        @Test
        @DisplayName("should return empty for non-existent role")
        void shouldReturnEmptyForMissingRole() {
            assertThat(roleRepository.findByName("ROLE_NONEXISTENT")).isEmpty();
        }
    }

    @Nested
    @DisplayName("DepartmentRepository")
    class DepartmentRepositoryTests {

        @Test
        @DisplayName("should save and find department by dept code")
        void shouldFindByDeptCode() {
            Department dept = Department.builder()
                    .deptCode("EE")
                    .deptName("Electrical Engineering")
                    .description("EE Dept")
                    .headOfDept("Dr. Volt")
                    .build();
            departmentRepository.save(dept);

            Optional<Department> found = departmentRepository.findByDeptCode("EE");
            assertThat(found).isPresent();
            assertThat(found.get().getDeptName()).isEqualTo("Electrical Engineering");
        }

        @Test
        @DisplayName("should check if dept code exists")
        void shouldCheckDeptCodeExists() {
            Department dept = Department.builder()
                    .deptCode("ME")
                    .deptName("Mechanical Engineering")
                    .description("ME Dept")
                    .headOfDept("Dr. Gear")
                    .build();
            departmentRepository.save(dept);

            assertThat(departmentRepository.existsByDeptCode("ME")).isTrue();
            assertThat(departmentRepository.existsByDeptCode("XX")).isFalse();
        }

        @Test
        @DisplayName("should list all departments")
        void shouldListAll() {
            departmentRepository.save(Department.builder()
                    .deptCode("PHY").deptName("Physics").description("P").headOfDept("Dr. A").build());
            departmentRepository.save(Department.builder()
                    .deptCode("CHE").deptName("Chemistry").description("C").headOfDept("Dr. B").build());

            List<Department> all = departmentRepository.findAll();
            assertThat(all).hasSizeGreaterThanOrEqualTo(2);
        }
    }
}
