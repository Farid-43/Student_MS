package com.example.StudentMS.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStudentRequest {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String studentId;
    private String department;
    private Integer semester;
    private LocalDate enrollmentDate;
    private BigDecimal gpa;
}
