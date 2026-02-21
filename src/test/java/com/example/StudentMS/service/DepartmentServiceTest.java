package com.example.StudentMS.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import com.example.StudentMS.dto.DepartmentDTO;
import com.example.StudentMS.entity.Department;
import com.example.StudentMS.repository.DepartmentRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentService Unit Tests")
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department department;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(1L)
                .deptCode("CSE")
                .deptName("Computer Science and Engineering")
                .description("CSE Department")
                .headOfDept("Dr. Rahman")
                .build();
    }

    @Nested
    @DisplayName("getAllDepartments")
    class GetAllDepartments {

        @Test
        @DisplayName("should return all departments")
        void shouldReturnAllDepartments() {
            Department dept2 = Department.builder()
                    .id(2L).deptCode("EEE").deptName("Electrical Engineering").build();

            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department, dept2));

            List<DepartmentDTO> result = departmentService.getAllDepartments();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getDeptCode()).isEqualTo("CSE");
            assertThat(result.get(1).getDeptCode()).isEqualTo("EEE");
        }
    }

    @Nested
    @DisplayName("getDepartmentById")
    class GetDepartmentById {

        @Test
        @DisplayName("should return department when found")
        void shouldReturnDepartment() {
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

            DepartmentDTO result = departmentService.getDepartmentById(1L);

            assertThat(result.getDeptCode()).isEqualTo("CSE");
            assertThat(result.getDeptName()).isEqualTo("Computer Science and Engineering");
            assertThat(result.getHeadOfDept()).isEqualTo("Dr. Rahman");
        }

        @Test
        @DisplayName("should throw when department not found")
        void shouldThrowWhenNotFound() {
            when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> departmentService.getDepartmentById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Department not found");
        }
    }

    @Nested
    @DisplayName("createDepartment")
    class CreateDepartment {

        @Test
        @DisplayName("should create department successfully")
        void shouldCreateDepartment() {
            DepartmentDTO dto = DepartmentDTO.builder()
                    .deptCode("ME").deptName("Mechanical Engineering")
                    .description("ME Dept").headOfDept("Dr. Ahmed").build();

            when(departmentRepository.existsByDeptCode("ME")).thenReturn(false);
            when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> {
                Department saved = inv.getArgument(0);
                saved.setId(2L);
                return saved;
            });

            DepartmentDTO result = departmentService.createDepartment(dto);

            assertThat(result.getDeptCode()).isEqualTo("ME");
            assertThat(result.getDeptName()).isEqualTo("Mechanical Engineering");
            verify(departmentRepository).save(any(Department.class));
        }

        @Test
        @DisplayName("should throw when code already exists")
        void shouldThrowWhenDuplicateCode() {
            DepartmentDTO dto = DepartmentDTO.builder().deptCode("CSE").deptName("Test").build();
            when(departmentRepository.existsByDeptCode("CSE")).thenReturn(true);

            assertThatThrownBy(() -> departmentService.createDepartment(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Department code already exists");
        }
    }

    @Nested
    @DisplayName("updateDepartment")
    class UpdateDepartment {

        @Test
        @DisplayName("should update department successfully")
        void shouldUpdateDepartment() {
            DepartmentDTO dto = DepartmentDTO.builder()
                    .deptCode("CSE").deptName("Updated CS").description("Updated").headOfDept("Dr. New").build();

            when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
            when(departmentRepository.save(any(Department.class))).thenReturn(department);

            DepartmentDTO result = departmentService.updateDepartment(1L, dto);

            assertThat(result).isNotNull();
            verify(departmentRepository).save(any(Department.class));
        }
    }

    @Nested
    @DisplayName("deleteDepartment")
    class DeleteDepartment {

        @Test
        @DisplayName("should delete department successfully")
        void shouldDeleteDepartment() {
            when(departmentRepository.existsById(1L)).thenReturn(true);

            departmentService.deleteDepartment(1L);

            verify(departmentRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when deleting non-existent department")
        void shouldThrowWhenNotFound() {
            when(departmentRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> departmentService.deleteDepartment(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Department not found");
        }
    }
}
