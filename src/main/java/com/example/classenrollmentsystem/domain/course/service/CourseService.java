package com.example.classenrollmentsystem.domain.course.service;

import com.example.classenrollmentsystem.domain.course.dto.request.CreateCourseRequest;
import com.example.classenrollmentsystem.domain.course.dto.request.UpdateCourseStatusRequest;
import com.example.classenrollmentsystem.domain.course.dto.response.CourseDetailResponse;
import com.example.classenrollmentsystem.domain.course.dto.response.CourseListResponse;
import com.example.classenrollmentsystem.domain.course.dto.response.CreateCourseResponse;
import com.example.classenrollmentsystem.domain.course.dto.response.UpdateCourseStatusResponse;
import com.example.classenrollmentsystem.domain.course.entity.Course;
import com.example.classenrollmentsystem.domain.course.entity.CourseStatus;
import com.example.classenrollmentsystem.domain.course.repository.CourseRepository;
import com.example.classenrollmentsystem.domain.user.entity.User;
import com.example.classenrollmentsystem.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateCourseResponse createCourse(Long userId, CreateCourseRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow();

        Course course = Course.create(
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getCapacity(),
                request.getStartDate(),
                request.getEndDate(),
                user
        );

        Course savedCourse = courseRepository.save(course);

        return new CreateCourseResponse(savedCourse.getId(), savedCourse.getTitle());
    }

    @Transactional
    public UpdateCourseStatusResponse updateCourseStatus(Long userId, Long courseId, UpdateCourseStatusRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow();

        if (!course.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException();
        }

        course.updateStatus(request.getStatus());

        return new UpdateCourseStatusResponse(course.getId(), course.getStatus());
    }

    public List<CourseListResponse> getCourses(CourseStatus status) {
        List<Course> courses;

        if (status == null) {
            courses = courseRepository.findAll();
        } else {
            courses = courseRepository.findByStatus(status);
        }

        return courses.stream()
                .map(CourseListResponse::from)
                .toList();
    }

    public CourseDetailResponse getCourseDetail(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow();

        return CourseDetailResponse.from(course);
    }
}
