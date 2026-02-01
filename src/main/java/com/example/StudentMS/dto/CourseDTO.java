package com.example.StudentMS.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {
    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private Integer credits;
    private Long teacherId;
    private String teacherName;
}
