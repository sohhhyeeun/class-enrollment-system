package com.example.classenrollmentsystem.domain.enrollment.service;

import com.example.classenrollmentsystem.domain.course.entity.Course;
import com.example.classenrollmentsystem.domain.course.entity.CourseStatus;
import com.example.classenrollmentsystem.domain.course.repository.CourseRepository;
import com.example.classenrollmentsystem.domain.enrollment.dto.response.ConfirmEnrollmentResponse;
import com.example.classenrollmentsystem.domain.enrollment.dto.response.CreateEnrollmentResponse;
import com.example.classenrollmentsystem.domain.enrollment.entity.Enrollment;
import com.example.classenrollmentsystem.domain.enrollment.entity.EnrollmentStatus;
import com.example.classenrollmentsystem.domain.enrollment.repository.EnrollmentRepository;
import com.example.classenrollmentsystem.domain.user.entity.User;
import com.example.classenrollmentsystem.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public CreateEnrollmentResponse createEnrollment(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow();
        Course course = courseRepository.findById(courseId)
                .orElseThrow();

        // 본인 강의 신청 불가
        if (course.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException();
        }

        // OPEN 상태 강의만 신청 가능
        if (course.getStatus() != CourseStatus.OPEN) {
            throw new IllegalArgumentException();
        }

        // 이미 신청한 강의는 재신청 불가
        boolean alreadyEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
        if (alreadyEnrolled) {
            throw new IllegalArgumentException();
        }

        Enrollment enrollment = Enrollment.create(user, course);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        return new CreateEnrollmentResponse(savedEnrollment.getId());
    }

    public ConfirmEnrollmentResponse confirmEnrollment(Long userId, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow();

        // 본인 신청만 수강 확정 가능
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException();
        }

        // PENDING 상태 신청만 수강 확정 가능
        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new IllegalArgumentException();
        }

        Course course = enrollment.getCourse();

        // OPEN 상태 강의만 수강 확정 가능
        if (course.getStatus() != CourseStatus.OPEN) {
            throw new IllegalArgumentException();
        }

        // 정원 초과 검증
        if (course.getCurrentEnrollmentCount() >= course.getCapacity()) {
            throw new IllegalArgumentException();
        }

        enrollment.confirm();
        course.increaseEnrollmentCount();

        return new ConfirmEnrollmentResponse(enrollment.getId());
    }
}
