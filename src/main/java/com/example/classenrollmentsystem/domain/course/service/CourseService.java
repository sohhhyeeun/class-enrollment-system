package com.example.classenrollmentsystem.domain.course.service;

import com.example.classenrollmentsystem.domain.course.dto.request.CreateCourseRequest;
import com.example.classenrollmentsystem.domain.course.dto.request.UpdateCourseStatusRequest;
import com.example.classenrollmentsystem.domain.course.dto.response.CourseDetailResponse;
import com.example.classenrollmentsystem.domain.course.dto.response.CourseListPageResponse;
import com.example.classenrollmentsystem.domain.course.dto.response.CreateCourseResponse;
import com.example.classenrollmentsystem.domain.course.dto.response.UpdateCourseStatusResponse;
import com.example.classenrollmentsystem.domain.course.entity.Course;
import com.example.classenrollmentsystem.domain.course.entity.CourseStatus;
import com.example.classenrollmentsystem.domain.course.repository.CourseRepository;
import com.example.classenrollmentsystem.domain.user.entity.User;
import com.example.classenrollmentsystem.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

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
        Course course = courseRepository.findByIdWithPessimisticLock(courseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        if (!course.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("강의 상태 변경 권한이 없습니다.");
        }

        course.updateStatus(request.getStatus());

        return new UpdateCourseStatusResponse(course.getId(), course.getStatus());
    }

    public CourseListPageResponse getCourses(CourseStatus status, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상만 가능합니다.");
        }

        if (size < 1 || size > 50) {
            throw new IllegalArgumentException("페이지 크기는 1 이상 50 이하만 가능합니다.");
        }

        Page<Course> courses = courseRepository.findCoursesWithUser(status, PageRequest.of(page, size));

        return CourseListPageResponse.from(courses);
    }

    public CourseDetailResponse getCourseDetail(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        return CourseDetailResponse.from(course);
    }
}
