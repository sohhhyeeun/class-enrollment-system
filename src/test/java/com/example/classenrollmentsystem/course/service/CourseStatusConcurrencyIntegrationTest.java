package com.example.classenrollmentsystem.course.service;

import com.example.classenrollmentsystem.domain.course.dto.request.UpdateCourseStatusRequest;
import com.example.classenrollmentsystem.domain.course.entity.Course;
import com.example.classenrollmentsystem.domain.course.entity.CourseStatus;
import com.example.classenrollmentsystem.domain.course.repository.CourseRepository;
import com.example.classenrollmentsystem.domain.course.service.CourseService;
import com.example.classenrollmentsystem.domain.enrollment.entity.Enrollment;
import com.example.classenrollmentsystem.domain.enrollment.entity.EnrollmentStatus;
import com.example.classenrollmentsystem.domain.enrollment.repository.EnrollmentRepository;
import com.example.classenrollmentsystem.domain.enrollment.service.EnrollmentService;
import com.example.classenrollmentsystem.domain.user.entity.User;
import com.example.classenrollmentsystem.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CourseStatusConcurrencyIntegrationTest {
    @Autowired
    private CourseService courseService;
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long courseId;
    private Long creatorId;
    private Long classmateId;

    @AfterEach
    void afterEach() {
        if (classmateId != null) {
            enrollmentRepository.findAllByUserId(classmateId).forEach(e -> enrollmentRepository.deleteById(e.getId()));
        }
        if (courseId != null) courseRepository.deleteById(courseId);
        if (creatorId != null) userRepository.deleteById(creatorId);
        if (classmateId != null) userRepository.deleteById(classmateId);
    }

    @Test
    @DisplayName("강의 상태 변경과 수강 확정 요청이 동시에 와도 수강 인원은 확정 건수와 일치한다.")
    void updateCourseStatusDoesNotOverwriteEnrollmentCount() throws Exception {
        // given
        User creator = userRepository.save(User.create("강사01"));
        User classmate = userRepository.save(User.create("수강생01"));
        creatorId = creator.getId();
        classmateId = classmate.getId();

        Course course = Course.create(
                "강의 제목01",
                "강의 설명01",
                10000,
                2,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                creator
        );
        course.updateStatus(CourseStatus.OPEN);
        course.increaseEnrollmentCount();
        course = courseRepository.save(course);
        courseId = course.getId();

        Long enrollmentId = enrollmentRepository.save(Enrollment.create(classmate, course)).getId();

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        CountDownLatch courseRead = new CountDownLatch(1);
        CountDownLatch confirmDone = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // when
        Future<?> statusChange = executor.submit(() -> transactionTemplate.executeWithoutResult(status -> {
            courseService.updateCourseStatus(creatorId, courseId, new UpdateCourseStatusRequest(CourseStatus.CLOSED));
            courseRead.countDown();
            awaitQuietly(confirmDone, 3);
        }));

        assertThat(courseRead.await(5, TimeUnit.SECONDS)).isTrue();
        Future<?> confirm = executor.submit(() -> {
            try {
                enrollmentService.confirmEnrollment(classmateId, enrollmentId);
            } catch (IllegalArgumentException ignored) {
            } finally {
                confirmDone.countDown();
            }
        });

        statusChange.get(10, TimeUnit.SECONDS);
        confirm.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        long confirmedCount = enrollmentRepository.findAllByUserId(classmateId).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.CONFIRMED)
                .count();
        Course result = courseRepository.findById(courseId).orElseThrow();

        assertThat(result.getCurrentEnrollmentCount()).isEqualTo(1 + confirmedCount);
    }

    private static void awaitQuietly(CountDownLatch latch, long seconds) {
        try {
            latch.await(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
