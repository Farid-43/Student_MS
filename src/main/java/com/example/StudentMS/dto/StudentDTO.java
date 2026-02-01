package com.example.StudentMS.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDTO {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String studentId;
    private String department;
    private Integer semester;
    private LocalDate enrollmentDate;
    private BigDecimal gpa;
}
