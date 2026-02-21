package com.example.StudentMS.service;

import com.example.StudentMS.dto.CourseDTO;
import com.example.StudentMS.entity.Course;
import com.example.StudentMS.entity.Teacher;
import com.example.StudentMS.entity.User;
import com.example.StudentMS.repository.CourseRepository;
import com.example.StudentMS.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService Unit Tests")
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private CourseService courseService;

    private Course course;
    private Teacher teacher;
    private User teacherUser;

    @BeforeEach
    void setUp() {
        teacherUser = User.builder()
                .id(1L)
                .username("teacher1")
                .email("teacher@test.com")
                .password("password")
                .firstName("John")
                .lastName("Smith")
                .build();

        teacher = Teacher.builder()
                .id(1L)
                .user(teacherUser)
                .employeeId("EMP001")
                .department("CSE")
                .designation("Professor")
                .build();

        course = Course.builder()
                .id(1L)
                .courseCode("CSE101")
                .courseName("Intro to CS")
                .description("Basic computer science course")
                .credits(3)
                .teacher(teacher)
                .build();
    }

    @Nested
    @DisplayName("getAllCourses")
    class GetAllCourses {

        @Test
        @DisplayName("should return all courses")
        void shouldReturnAllCourses() {
            Course course2 = Course.builder()
                    .id(2L).courseCode("CSE102").courseName("Data Structures")
                    .credits(3).teacher(teacher).build();

            when(courseRepository.findAll()).thenReturn(Arrays.asList(course, course2));

            List<CourseDTO> result = courseService.getAllCourses();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getCourseCode()).isEqualTo("CSE101");
            assertThat(result.get(1).getCourseCode()).isEqualTo("CSE102");
            verify(courseRepository).findAll();
        }

        @Test
        @DisplayName("should return empty list when no courses exist")
        void shouldReturnEmptyList() {
            when(courseRepository.findAll()).thenReturn(List.of());

            List<CourseDTO> result = courseService.getAllCourses();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCourseById")
    class GetCourseById {

        @Test
        @DisplayName("should return course when found")
        void shouldReturnCourseWhenFound() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            CourseDTO result = courseService.getCourseById(1L);

            assertThat(result.getCourseCode()).isEqualTo("CSE101");
            assertThat(result.getCourseName()).isEqualTo("Intro to CS");
            assertThat(result.getTeacherName()).isEqualTo("John Smith");
        }

        @Test
        @DisplayName("should throw exception when course not found")
        void shouldThrowWhenNotFound() {
            when(courseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.getCourseById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Course not found");
        }
    }

    @Nested
    @DisplayName("createCourse")
    class CreateCourse {

        @Test
        @DisplayName("should create course successfully")
        void shouldCreateCourse() {
            CourseDTO dto = CourseDTO.builder()
                    .courseCode("CSE201").courseName("Algorithms")
                    .description("Algorithm design").credits(3).teacherId(1L)
                    .build();

            when(courseRepository.existsByCourseCode("CSE201")).thenReturn(false);
            when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
                Course saved = invocation.getArgument(0);
                saved.setId(2L);
                return saved;
            });

            CourseDTO result = courseService.createCourse(dto);

            assertThat(result.getCourseName()).isEqualTo("Algorithms");
            verify(courseRepository).save(any(Course.class));
        }

        @Test
        @DisplayName("should throw when course code already exists")
        void shouldThrowWhenDuplicateCode() {
            CourseDTO dto = CourseDTO.builder().courseCode("CSE101").courseName("Test").build();
            when(courseRepository.existsByCourseCode("CSE101")).thenReturn(true);

            assertThatThrownBy(() -> courseService.createCourse(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Course code already exists!");
        }

        @Test
        @DisplayName("should create course without teacher")
        void shouldCreateCourseWithoutTeacher() {
            CourseDTO dto = CourseDTO.builder()
                    .courseCode("GEN101").courseName("General Studies")
                    .credits(2).build();

            when(courseRepository.existsByCourseCode("GEN101")).thenReturn(false);
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
                Course saved = invocation.getArgument(0);
                saved.setId(3L);
                return saved;
            });

            CourseDTO result = courseService.createCourse(dto);

            assertThat(result.getCourseName()).isEqualTo("General Studies");
            assertThat(result.getTeacherId()).isNull();
            verify(teacherRepository, never()).findById(anyLong());
        }
    }

    @Nested
    @DisplayName("updateCourse")
    class UpdateCourse {

        @Test
        @DisplayName("should update course successfully")
        void shouldUpdateCourse() {
            CourseDTO dto = CourseDTO.builder()
                    .courseName("Updated CS").description("Updated desc")
                    .credits(4).teacherId(1L).build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
            when(courseRepository.save(any(Course.class))).thenReturn(course);

            CourseDTO result = courseService.updateCourse(1L, dto);

            assertThat(result).isNotNull();
            verify(courseRepository).save(any(Course.class));
        }

        @Test
        @DisplayName("should throw when updating non-existent course")
        void shouldThrowWhenCourseNotFound() {
            CourseDTO dto = CourseDTO.builder().courseName("Test").build();
            when(courseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.updateCourse(99L, dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Course not found");
        }
    }

    @Nested
    @DisplayName("deleteCourse")
    class DeleteCourse {

        @Test
        @DisplayName("should delete course successfully")
        void shouldDeleteCourse() {
            when(courseRepository.existsById(1L)).thenReturn(true);

            courseService.deleteCourse(1L);

            verify(courseRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when deleting non-existent course")
        void shouldThrowWhenCourseNotFound() {
            when(courseRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> courseService.deleteCourse(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Course not found");
        }
    }
}
