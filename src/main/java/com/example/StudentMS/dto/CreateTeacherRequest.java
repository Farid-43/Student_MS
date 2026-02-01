package com.example.StudentMS.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTeacherRequest {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String employeeId;
    private String department;
    private String designation;
    private LocalDate joiningDate;
}
