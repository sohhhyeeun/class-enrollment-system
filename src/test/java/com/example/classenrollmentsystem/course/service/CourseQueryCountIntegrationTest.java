package com.example.classenrollmentsystem.course.service;

import com.example.classenrollmentsystem.domain.course.entity.Course;
import com.example.classenrollmentsystem.domain.course.repository.CourseRepository;
import com.example.classenrollmentsystem.domain.course.service.CourseService;
import com.example.classenrollmentsystem.domain.user.entity.User;
import com.example.classenrollmentsystem.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
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
class CourseQueryCountIntegrationTest {
    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private final List<Long> courseIds = new ArrayList<>();
    private final List<Long> userIds = new ArrayList<>();

    @AfterEach
    void afterEach() {
        courseIds.forEach(courseRepository::deleteById);
        userIds.forEach(userRepository::deleteById);
    }

    @Test
    @DisplayName("강의 개설자가 모두 달라도 강의 목록 조회 시 강의 개설자 조회로 인한 추가 쿼리(N+1)가 발생하지 않는다.")
    void getCoursesDoesNotCauseNPlusOne() {
        // given
        for (int i = 0; i < 5; i++) {
            User creator = userRepository.save(User.create("강사" + i));
            userIds.add(creator.getId());

            Course course = Course.create(
                    "강의 제목" + i,
                    "강의 설명" + i,
                    10000,
                    30,
                    LocalDate.now(),
                    LocalDate.now().plusDays(30),
                    creator
            );
            course = courseRepository.save(course);
            courseIds.add(course.getId());
        }

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        // when
        courseService.getCourses(null);

        // then
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }
}
