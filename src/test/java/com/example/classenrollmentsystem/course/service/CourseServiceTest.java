package com.example.classenrollmentsystem.course.service;

import com.example.classenrollmentsystem.domain.course.dto.request.CreateCourseRequest;
import com.example.classenrollmentsystem.domain.course.dto.request.UpdateCourseStatusRequest;
import com.example.classenrollmentsystem.domain.course.dto.response.CreateCourseResponse;
import com.example.classenrollmentsystem.domain.course.dto.response.UpdateCourseStatusResponse;
import com.example.classenrollmentsystem.domain.course.entity.Course;
import com.example.classenrollmentsystem.domain.course.entity.CourseStatus;
import com.example.classenrollmentsystem.domain.course.repository.CourseRepository;
import com.example.classenrollmentsystem.domain.course.service.CourseService;
import com.example.classenrollmentsystem.domain.user.entity.User;
import com.example.classenrollmentsystem.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    @InjectMocks
    private CourseService courseService;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("강의를 등록할 수 있다.")
    void createCourse() {
        // given
        Long userId = 1L;

        User user = User.create("사용자01");

        CreateCourseRequest request = new CreateCourseRequest(
                "강의 제목01",
                "강의 설명01",
                10000,
                30,
                LocalDate.now(),
                LocalDate.now().plusDays(30)
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        Course course = Course.create(
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getCapacity(),
                request.getStartDate(),
                request.getEndDate(),
                user
        );

        given(courseRepository.save(any(Course.class))).willReturn(course);

        // when
        CreateCourseResponse response = courseService.createCourse(userId, request);

        // then
        assertThat(response.getTitle()).isEqualTo("강의 제목01");
    }

    @Test
    @DisplayName("강의 상태를 변경할 수 있다.")
    void updateCourseStatus() {
        // given
        Long userId = 1L;
        Long courseId = 1L;

        User user = User.create("사용자01");

        ReflectionTestUtils.setField(user, "id", userId);

        Course course = Course.create(
                "강의 제목01",
                "강의 설명01",
                10000,
                30,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                user
        );

        UpdateCourseStatusRequest request = new UpdateCourseStatusRequest(CourseStatus.OPEN);

        given(courseRepository.findById(courseId)).willReturn(Optional.of(course));

        // when
        UpdateCourseStatusResponse response = courseService.updateCourseStatus(userId, courseId, request);

        // then
        assertThat(response.getStatus()).isEqualTo(CourseStatus.OPEN);
    }
}