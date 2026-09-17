package com.example.classenrollmentsystem.enrollment.service;

import com.example.classenrollmentsystem.domain.course.entity.Course;
import com.example.classenrollmentsystem.domain.course.entity.CourseStatus;
import com.example.classenrollmentsystem.domain.course.repository.CourseRepository;
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

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EnrollmentConcurrencyIntegrationTest {
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private UserRepository userRepository;

    private Long courseId;
    private Long creatorId;
    private Long classmate01Id;
    private Long classmate02Id;

    @AfterEach
    void afterEach() {
        if (courseId != null) {
            enrollmentRepository.findAllByUserId(classmate01Id).forEach(e -> enrollmentRepository.deleteById(e.getId()));
            if (classmate02Id != null) {
                enrollmentRepository.findAllByUserId(classmate02Id).forEach(e -> enrollmentRepository.deleteById(e.getId()));
            }
            courseRepository.deleteById(courseId);
        }
        if (creatorId != null) userRepository.deleteById(creatorId);
        if (classmate01Id != null) userRepository.deleteById(classmate01Id);
        if (classmate02Id != null) userRepository.deleteById(classmate02Id);
    }

    @Test
    @DisplayName("같은 신청 건에 동시에 확정 요청이 와도 정원은 한 번만 증가한다.")
    void confirmEnrollmentConcurrentlyBySameRequest() throws InterruptedException {
        // given
        User creator = userRepository.save(User.create("강사01"));
        User classmate01 = userRepository.save(User.create("수강생01"));
        creatorId = creator.getId();
        classmate01Id = classmate01.getId();

        Course course = Course.create(
                "강의 제목01",
                "강의 설명01",
                10000,
                30,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                creator
        );
        course.updateStatus(CourseStatus.OPEN);
        course = courseRepository.save(course);
        courseId = course.getId();

        Enrollment enrollment = enrollmentRepository.save(Enrollment.create(classmate01, course));
        Long enrollmentId = enrollment.getId();

        // when
        runConcurrently(() -> enrollmentService.confirmEnrollment(classmate01.getId(), enrollmentId));

        // then
        Course result = courseRepository.findById(courseId).orElseThrow();
        assertThat(result.getCurrentEnrollmentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 신청 건에 동시에 취소 요청이 와도 정원은 한 번만 감소한다.")
    void cancelEnrollmentConcurrentlyBySameRequest() throws InterruptedException {
        // given
        User creator = userRepository.save(User.create("강사01"));
        User classmate01 = userRepository.save(User.create("수강생01"));
        creatorId = creator.getId();
        classmate01Id = classmate01.getId();

        Course course = Course.create(
                "강의 제목01",
                "강의 설명01",
                10000,
                30,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                creator
        );
        course.updateStatus(CourseStatus.OPEN);
        for (int i = 0; i < 5; i++) {
            course.increaseEnrollmentCount();
        }
        course = courseRepository.save(course);
        courseId = course.getId();

        Enrollment enrollment = Enrollment.create(classmate01, course);
        enrollment.confirm();
        enrollment = enrollmentRepository.save(enrollment);
        Long enrollmentId = enrollment.getId();

        // when
        runConcurrently(() -> enrollmentService.cancelEnrollment(classmate01.getId(), enrollmentId));

        // then
        Course result = courseRepository.findById(courseId).orElseThrow();
        assertThat(result.getCurrentEnrollmentCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("서로 다른 사용자가 동시에 확정 요청을 해도 정원은 초과하지 않는다.")
    void confirmEnrollmentConcurrentlyByDifferentUsers() throws InterruptedException {
        // given
        User creator = userRepository.save(User.create("강사01"));
        User classmate01 = userRepository.save(User.create("수강생01"));
        User classmate02 = userRepository.save(User.create("수강생02"));
        creatorId = creator.getId();
        classmate01Id = classmate01.getId();
        classmate02Id = classmate02.getId();

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

        Long enrollmentAId = enrollmentRepository.save(Enrollment.create(classmate01, course)).getId();
        Long enrollmentBId = enrollmentRepository.save(Enrollment.create(classmate02, course)).getId();

        // when
        runConcurrently(
                () -> enrollmentService.confirmEnrollment(classmate01.getId(), enrollmentAId),
                () -> enrollmentService.confirmEnrollment(classmate02.getId(), enrollmentBId)
        );

        // then
        Course result = courseRepository.findById(courseId).orElseThrow();
        assertThat(result.getCurrentEnrollmentCount()).isEqualTo(2);

        long confirmedCount = enrollmentRepository.findAllByUserId(classmate01.getId()).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.CONFIRMED)
                .count()
                + enrollmentRepository.findAllByUserId(classmate02.getId()).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.CONFIRMED)
                .count();
        assertThat(confirmedCount).isEqualTo(1);
    }

    private void runConcurrently(Runnable action) throws InterruptedException {
        runConcurrently(action, action);
    }

    private void runConcurrently(Runnable action1, Runnable action2) throws InterruptedException {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        Runnable[] actions = {action1, action2};

        for (int i = 0; i < threadCount; i++) {
            Runnable action = actions[i];
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    action.run();
                } catch (Exception ignored) {

                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
    }
}
