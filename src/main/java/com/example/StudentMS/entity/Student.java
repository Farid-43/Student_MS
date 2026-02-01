package com.example.StudentMS.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "student_id", nullable = false, unique = true, length = 20)
    private String studentId;

    @Column(length = 100)
    private String department;

    private Integer semester;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    @Column(precision = 3, scale = 2)
    private BigDecimal gpa;
}
