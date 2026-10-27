package com.example.classenrollmentsystem.enrollment.controller;

import com.example.classenrollmentsystem.domain.course.entity.Course;
import com.example.classenrollmentsystem.domain.course.entity.CourseStatus;
import com.example.classenrollmentsystem.domain.course.repository.CourseRepository;
import com.example.classenrollmentsystem.domain.enrollment.repository.EnrollmentRepository;
import com.example.classenrollmentsystem.domain.user.entity.User;
import com.example.classenrollmentsystem.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EnrollmentDuplicateHttpConcurrencyIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private UserRepository userRepository;

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
    @DisplayName("같은 사용자가 같은 강의에 동시에 신청 요청을 보내도 1건만 성공하고 5xx 응답이 발생하지 않는다.")
    void postEnrollmentConcurrentlyBySameUser() throws InterruptedException {
        // given
        User creator = userRepository.save(User.create("강사01"));
        User classmate = userRepository.save(User.create("수강생01"));
        creatorId = creator.getId();
        classmateId = classmate.getId();

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

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", String.valueOf(classmateId));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        String url = "/api/v1/courses/" + courseId + "/enrollments";

        List<ResponseEntity<String>> responses = Collections.synchronizedList(new ArrayList<>());

        // when
        boolean completed = runConcurrently(() -> {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            responses.add(response);
        });

        // then
        long successCount = responses.stream()
                .filter(r -> r.getStatusCode().is2xxSuccessful())
                .count();
        long serverErrorCount = responses.stream()
                .filter(r -> r.getStatusCode().is5xxServerError())
                .count();

        assertThat(completed).isTrue();
        assertThat(responses).hasSize(10);
        assertThat(successCount).isEqualTo(1);
        assertThat(serverErrorCount).isEqualTo(0);
        assertThat(enrollmentRepository.findAllByUserId(classmateId)).hasSize(1);
    }

    private boolean runConcurrently(Runnable action) throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
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
        boolean completed = doneLatch.await(20, TimeUnit.SECONDS);
        executor.shutdown();

        return completed;
    }
}
