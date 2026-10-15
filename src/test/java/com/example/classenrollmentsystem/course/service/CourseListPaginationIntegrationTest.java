package com.example.classenrollmentsystem.course.service;

import com.example.classenrollmentsystem.domain.course.dto.response.CourseListPageResponse;
import com.example.classenrollmentsystem.domain.course.entity.Course;
import com.example.classenrollmentsystem.domain.course.repository.CourseRepository;
import com.example.classenrollmentsystem.domain.course.service.CourseService;
import com.example.classenrollmentsystem.domain.user.entity.User;
import com.example.classenrollmentsystem.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CourseListPaginationIntegrationTest {
    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private UserRepository userRepository;

    private final List<Long> courseIds = new ArrayList<>();
    private Long creatorId;

    @AfterEach
    void afterEach() {
        courseIds.forEach(courseRepository::deleteById);
        if (creatorId != null) userRepository.deleteById(creatorId);
    }

    @Test
    @DisplayName("강의 목록은 최근에 등록된 강의부터 페이지 경계에서 중복이나 누락 없이 조회된다.")
    void getCoursesByLatestOrderWithPaging() {
        // given
        User creator = userRepository.save(User.create("강사01"));
        creatorId = creator.getId();

        for (int i = 1; i <= 3; i++) {
            Course course = Course.create(
                    "강의 제목0" + i,
                    "강의 설명0" + i,
                    10000,
                    30,
                    LocalDate.now(),
                    LocalDate.now().plusDays(30),
                    creator
            );
            courseIds.add(courseRepository.save(course).getId());
        }

        Long firstCourseId = courseIds.get(0);
        Long secondCourseId = courseIds.get(1);
        Long thirdCourseId = courseIds.get(2);

        // when
        CourseListPageResponse firstPage = courseService.getCourses(null, 0, 2);
        CourseListPageResponse secondPage = courseService.getCourses(null, 1, 2);

        // then
        assertThat(firstPage.getCourses().get(0).getCourseId()).isEqualTo(thirdCourseId);
        assertThat(firstPage.getCourses().get(1).getCourseId()).isEqualTo(secondCourseId);
        assertThat(secondPage.getCourses().get(0).getCourseId()).isEqualTo(firstCourseId);
    }
}
