package com.example.StudentMS.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.StudentMS.dto.DepartmentDTO;
import com.example.StudentMS.entity.Department;
import com.example.StudentMS.repository.DepartmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public DepartmentDTO getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        return toDTO(dept);
    }

    public DepartmentDTO getDepartmentByCode(String code) {
        Department dept = departmentRepository.findByDeptCode(code)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        return toDTO(dept);
    }

    public DepartmentDTO createDepartment(DepartmentDTO dto) {
        if (departmentRepository.existsByDeptCode(dto.getDeptCode())) {
            throw new RuntimeException("Department code already exists");
        }

        Department dept = Department.builder()
                .deptCode(dto.getDeptCode())
                .deptName(dto.getDeptName())
                .description(dto.getDescription())
                .headOfDept(dto.getHeadOfDept())
                .build();

        return toDTO(departmentRepository.save(dept));
    }

    public DepartmentDTO updateDepartment(Long id, DepartmentDTO dto) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        dept.setDeptCode(dto.getDeptCode());
        dept.setDeptName(dto.getDeptName());
        dept.setDescription(dto.getDescription());
        dept.setHeadOfDept(dto.getHeadOfDept());

        return toDTO(departmentRepository.save(dept));
    }

    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new RuntimeException("Department not found");
        }
        departmentRepository.deleteById(id);
    }

    private DepartmentDTO toDTO(Department dept) {
        return DepartmentDTO.builder()
                .id(dept.getId())
                .deptCode(dept.getDeptCode())
                .deptName(dept.getDeptName())
                .description(dept.getDescription())
                .headOfDept(dept.getHeadOfDept())
                .build();
    }
}
