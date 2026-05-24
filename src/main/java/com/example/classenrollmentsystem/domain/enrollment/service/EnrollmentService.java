package com.example.classenrollmentsystem.domain.enrollment.service;

import com.example.classenrollmentsystem.domain.course.entity.Course;
import com.example.classenrollmentsystem.domain.course.entity.CourseStatus;
import com.example.classenrollmentsystem.domain.course.repository.CourseRepository;
import com.example.classenrollmentsystem.domain.enrollment.dto.response.CancelEnrollmentResponse;
import com.example.classenrollmentsystem.domain.enrollment.dto.response.ConfirmEnrollmentResponse;
import com.example.classenrollmentsystem.domain.enrollment.dto.response.CreateEnrollmentResponse;
import com.example.classenrollmentsystem.domain.enrollment.dto.response.MyEnrollmentResponse;
import com.example.classenrollmentsystem.domain.enrollment.entity.Enrollment;
import com.example.classenrollmentsystem.domain.enrollment.entity.EnrollmentStatus;
import com.example.classenrollmentsystem.domain.enrollment.repository.EnrollmentRepository;
import com.example.classenrollmentsystem.domain.user.entity.User;
import com.example.classenrollmentsystem.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public CreateEnrollmentResponse createEnrollment(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        // 본인 강의 신청 불가
        if (course.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 개설한 강의는 신청할 수 없습니다.");
        }

        // OPEN 상태 강의만 신청 가능
        if (course.getStatus() != CourseStatus.OPEN) {
            throw new IllegalArgumentException("모집 중인 강의만 신청할 수 있습니다.");
        }

        // 이미 신청한 강의는 재신청 불가
        boolean alreadyEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
        if (alreadyEnrolled) {
            throw new IllegalArgumentException("이미 신청한 강의는 재신청할 수 없습니다.");
        }

        Enrollment enrollment = Enrollment.create(user, course);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        return new CreateEnrollmentResponse(savedEnrollment.getId());
    }

    public ConfirmEnrollmentResponse confirmEnrollment(Long userId, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 수강 신청입니다."));

        // 본인 신청만 수강 확정 가능
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("수강 확정 권한이 없습니다.");
        }

        // PENDING 상태 신청만 수강 확정 가능
        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new IllegalArgumentException("결제 대기 상태인 신청만 확정할 수 있습니다.");
        }

        Course course = courseRepository.findByIdWithPessimisticLock(enrollment.getCourse().getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        // OPEN 상태 강의만 수강 확정 가능
        if (course.getStatus() != CourseStatus.OPEN) {
            throw new IllegalArgumentException("모집 중인 강의만 확정할 수 있습니다.");
        }

        // 정원 초과 검증
        if (course.getCurrentEnrollmentCount() >= course.getCapacity()) {
            throw new IllegalArgumentException("수강 정원이 초과되었습니다.");
        }

        enrollment.confirm();
        course.increaseEnrollmentCount();

        return new ConfirmEnrollmentResponse(enrollment.getId());
    }

    public CancelEnrollmentResponse cancelEnrollment(Long userId, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 수강 신청입니다."));

        // 본인 신청만 수강 취소 가능
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("수강 취소 권한이 없습니다.");
        }

        // 이미 취소된 신청은 재취소 불가
        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new IllegalArgumentException("이미 취소된 신청은 재취소할 수 없습니다.");
        }

        EnrollmentStatus previousStatus = enrollment.getStatus();

        // CONFIRMED 상태 신청에만 정원 감소
        if (previousStatus == EnrollmentStatus.CONFIRMED) {
            Course course = courseRepository.findByIdWithPessimisticLock(enrollment.getCourse().getId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

            course.decreaseEnrollmentCount();
        }

        enrollment.cancel();

        return new CancelEnrollmentResponse(enrollment.getId());
    }

    @Transactional(readOnly = true)
    public List<MyEnrollmentResponse> getMyEnrollments(Long userId) {
        List<Enrollment> enrollments = enrollmentRepository.findAllByUserId(userId);

        return enrollments.stream()
                .map(MyEnrollmentResponse::from)
                .toList();
    }
}
