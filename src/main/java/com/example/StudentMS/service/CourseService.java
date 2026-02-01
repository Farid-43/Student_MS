package com.example.StudentMS.service;

import com.example.StudentMS.dto.CourseDTO;
import com.example.StudentMS.entity.Course;
import com.example.StudentMS.entity.Teacher;
import com.example.StudentMS.repository.CourseRepository;
import com.example.StudentMS.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;

    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return mapToDTO(course);
    }

    public CourseDTO getCourseByCourseCode(String courseCode) {
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return mapToDTO(course);
    }

    public List<CourseDTO> getCoursesByTeacherId(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseDTO createCourse(CourseDTO courseDTO) {
        if (courseRepository.existsByCourseCode(courseDTO.getCourseCode())) {
            throw new RuntimeException("Course code already exists!");
        }

        Course course = Course.builder()
                .courseCode(courseDTO.getCourseCode())
                .courseName(courseDTO.getCourseName())
                .description(courseDTO.getDescription())
                .credits(courseDTO.getCredits())
                .build();

        if (courseDTO.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(courseDTO.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            course.setTeacher(teacher);
        }

        Course savedCourse = courseRepository.save(course);
        return mapToDTO(savedCourse);
    }

    @Transactional
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        course.setCourseName(courseDTO.getCourseName());
        course.setDescription(courseDTO.getDescription());
        course.setCredits(courseDTO.getCredits());

        if (courseDTO.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(courseDTO.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            course.setTeacher(teacher);
        } else {
            course.setTeacher(null);
        }

        Course savedCourse = courseRepository.save(course);
        return mapToDTO(savedCourse);
    }

    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found");
        }
        courseRepository.deleteById(id);
    }

    private CourseDTO mapToDTO(Course course) {
        CourseDTO.CourseDTOBuilder builder = CourseDTO.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .credits(course.getCredits());

        if (course.getTeacher() != null) {
            builder.teacherId(course.getTeacher().getId());
            builder.teacherName(course.getTeacher().getUser().getFirstName() + " " +
                    course.getTeacher().getUser().getLastName());
        }

        return builder.build();
    }
}
