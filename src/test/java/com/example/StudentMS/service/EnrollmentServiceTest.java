package com.example.StudentMS.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
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

import com.example.StudentMS.dto.EnrollmentDTO;
import com.example.StudentMS.entity.Course;
import com.example.StudentMS.entity.Enrollment;
import com.example.StudentMS.entity.Student;
import com.example.StudentMS.entity.User;
import com.example.StudentMS.repository.CourseRepository;
import com.example.StudentMS.repository.EnrollmentRepository;
import com.example.StudentMS.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService Unit Tests")
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Student student;
    private Course course;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        User studentUser = User.builder()
                .id(1L).username("student1").email("s@test.com")
                .password("pass").firstName("Alice").lastName("Brown")
                .build();

        student = Student.builder()
                .id(1L).user(studentUser).studentId("STU001").department("CSE")
                .build();

        course = Course.builder()
                .id(1L).courseCode("CSE101").courseName("Intro to CS").credits(3)
                .build();

        enrollment = Enrollment.builder()
                .id(1L).student(student).course(course)
                .enrolledDate(LocalDate.now()).status("ENROLLED")
                .build();
    }

    @Nested
    @DisplayName("enrollStudent")
    class EnrollStudent {

        @Test
        @DisplayName("should enroll student successfully")
        void shouldEnrollStudent() {
            when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 1L)).thenReturn(false);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

            EnrollmentDTO result = enrollmentService.enrollStudent(1L, 1L);

            assertThat(result.getStudentId()).isEqualTo(1L);
            assertThat(result.getCourseCode()).isEqualTo("CSE101");
            assertThat(result.getStatus()).isEqualTo("ENROLLED");
            verify(enrollmentRepository).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("should throw when already enrolled")
        void shouldThrowWhenAlreadyEnrolled() {
            when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 1L)).thenReturn(true);

            assertThatThrownBy(() -> enrollmentService.enrollStudent(1L, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Student is already enrolled in this course");
        }

        @Test
        @DisplayName("should throw when student not found")
        void shouldThrowWhenStudentNotFound() {
            when(enrollmentRepository.existsByStudentIdAndCourseId(99L, 1L)).thenReturn(false);
            when(studentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.enrollStudent(99L, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Student not found");
        }

        @Test
        @DisplayName("should throw when course not found")
        void shouldThrowWhenCourseNotFound() {
            when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 99L)).thenReturn(false);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(courseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.enrollStudent(1L, 99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Course not found");
        }
    }

    @Nested
    @DisplayName("getEnrollmentsByStudentId")
    class GetEnrollmentsByStudentId {

        @Test
        @DisplayName("should return enrollments for student")
        void shouldReturnEnrollments() {
            when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(enrollment));

            List<EnrollmentDTO> result = enrollmentService.getEnrollmentsByStudentId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCourseCode()).isEqualTo("CSE101");
        }
    }

    @Nested
    @DisplayName("dropEnrollment")
    class DropEnrollment {

        @Test
        @DisplayName("should drop enrollment successfully")
        void shouldDropEnrollment() {
            when(enrollmentRepository.findByStudentIdAndCourseId(1L, 1L))
                    .thenReturn(Optional.of(enrollment));

            enrollmentService.dropEnrollment(1L, 1L);

            verify(enrollmentRepository).delete(enrollment);
        }

        @Test
        @DisplayName("should throw when enrollment not found")
        void shouldThrowWhenNotFound() {
            when(enrollmentRepository.findByStudentIdAndCourseId(1L, 99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.dropEnrollment(1L, 99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Enrollment not found");
        }
    }
}
