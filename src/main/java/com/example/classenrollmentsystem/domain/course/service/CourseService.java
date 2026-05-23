package com.example.classenrollmentsystem.domain.course.service;

import com.example.classenrollmentsystem.domain.course.dto.request.CreateCourseRequest;
import com.example.classenrollmentsystem.domain.course.dto.response.CreateCourseResponse;
import com.example.classenrollmentsystem.domain.course.entity.Course;
import com.example.classenrollmentsystem.domain.course.repository.CourseRepository;
import com.example.classenrollmentsystem.domain.user.entity.User;
import com.example.classenrollmentsystem.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return new CreateCourseResponse(savedCourse.getId());
    }
}
