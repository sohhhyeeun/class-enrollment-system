package com.example.classenrollmentsystem.enrollment.service;

import com.example.classenrollmentsystem.domain.course.entity.Course;
import com.example.classenrollmentsystem.domain.course.entity.CourseStatus;
import com.example.classenrollmentsystem.domain.course.repository.CourseRepository;
import com.example.classenrollmentsystem.domain.enrollment.dto.response.MyEnrollmentResponse;
import com.example.classenrollmentsystem.domain.enrollment.entity.Enrollment;
import com.example.classenrollmentsystem.domain.enrollment.repository.EnrollmentRepository;
import com.example.classenrollmentsystem.domain.enrollment.service.EnrollmentService;
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
class EnrollmentQueryCountIntegrationTest {
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private final List<Long> enrollmentIds = new ArrayList<>();
    private final List<Long> courseIds = new ArrayList<>();
    private final List<Long> userIds = new ArrayList<>();

    @AfterEach
    void afterEach() {
        enrollmentIds.forEach(enrollmentRepository::deleteById);
        courseIds.forEach(courseRepository::deleteById);
        userIds.forEach(userRepository::deleteById);
    }

    @Test
    @DisplayName("신청한 강의가 많아져도 내 수강 목록 조회 시 강의 조회로 인한 추가 쿼리(N+1)가 발생하지 않는다.")
    void getMyEnrollmentsQueryCountDoesNotDependOnEnrollmentCount() {
        // given
        User creator = userRepository.save(User.create("강사01"));
        userIds.add(creator.getId());

        List<Course> courses = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Course course = Course.create(
                    "강의 제목" + i,
                    "강의 설명" + i,
                    10000,
                    30,
                    LocalDate.now(),
                    LocalDate.now().plusDays(30),
                    creator
            );
            course.updateStatus(CourseStatus.OPEN);
            course = courseRepository.save(course);
            courseIds.add(course.getId());
            courses.add(course);
        }

        User classmateWithFive = userRepository.save(User.create("수강생01"));
        User classmateWithTen = userRepository.save(User.create("수강생02"));
        userIds.add(classmateWithFive.getId());
        userIds.add(classmateWithTen.getId());

        for (int i = 0; i < 5; i++) {
            enrollmentIds.add(enrollmentRepository.save(Enrollment.create(classmateWithFive, courses.get(i))).getId());
        }
        for (int i = 0; i < 10; i++) {
            enrollmentIds.add(enrollmentRepository.save(Enrollment.create(classmateWithTen, courses.get(i))).getId());
        }

        // when
        long countForFive = measurePrepareStatementCount(classmateWithFive.getId(), 5);
        long countForTen = measurePrepareStatementCount(classmateWithTen.getId(), 10);

        // then
        assertThat(countForTen).isEqualTo(countForFive);
    }

    private long measurePrepareStatementCount(Long userId, int expectedSize) {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        List<MyEnrollmentResponse> result = enrollmentService.getMyEnrollments(userId);

        long prepareStatementCount = statistics.getPrepareStatementCount();

        assertThat(result).hasSize(expectedSize);
        return prepareStatementCount;
    }
}
