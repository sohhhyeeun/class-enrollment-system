package com.example.classenrollmentsystem.enrollment.service;

import com.example.classenrollmentsystem.domain.course.entity.Course;
import com.example.classenrollmentsystem.domain.course.entity.CourseStatus;
import com.example.classenrollmentsystem.domain.course.repository.CourseRepository;
import com.example.classenrollmentsystem.domain.enrollment.dto.response.CancelEnrollmentResponse;
import com.example.classenrollmentsystem.domain.enrollment.dto.response.ConfirmEnrollmentResponse;
import com.example.classenrollmentsystem.domain.enrollment.dto.response.CreateEnrollmentResponse;
import com.example.classenrollmentsystem.domain.enrollment.entity.Enrollment;
import com.example.classenrollmentsystem.domain.enrollment.entity.EnrollmentStatus;
import com.example.classenrollmentsystem.domain.enrollment.repository.EnrollmentRepository;
import com.example.classenrollmentsystem.domain.enrollment.service.EnrollmentService;
import com.example.classenrollmentsystem.domain.user.entity.User;
import com.example.classenrollmentsystem.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {
    @InjectMocks
    private EnrollmentService enrollmentService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Test
    @DisplayName("수강 신청을 할 수 있다.")
    void createEnrollment() {
        // given
        Long userId = 1L;
        Long courseId = 1L;

        User classmate = User.create("수강생01");
        User creator = User.create("강사01");

        ReflectionTestUtils.setField(creator, "id", 2L);

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

        Enrollment enrollment = Enrollment.create(classmate, course);
        ReflectionTestUtils.setField(enrollment, "id", 1L);

        given(userRepository.findById(userId)).willReturn(Optional.of(classmate));
        given(courseRepository.findById(courseId)).willReturn(Optional.of(course));
        given(enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)).willReturn(false);
        given(enrollmentRepository.saveAndFlush(any(Enrollment.class))).willReturn(enrollment);

        // when
        CreateEnrollmentResponse response = enrollmentService.createEnrollment(userId, courseId);

        // then
        assertThat(response.getEnrollmentId()).isNotNull();
    }

    @Test
    @DisplayName("같은 강의에 동시에 신청해 중복 저장이 실패하면 이미 신청한 강의와 같은 예외가 발생한다.")
    void createEnrollmentWithDuplicateSave() {
        // given
        Long userId = 1L;
        Long courseId = 1L;

        User classmate = User.create("수강생01");
        User creator = User.create("강사01");

        ReflectionTestUtils.setField(creator, "id", 2L);

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

        given(userRepository.findById(userId)).willReturn(Optional.of(classmate));
        given(courseRepository.findById(courseId)).willReturn(Optional.of(course));
        given(enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)).willReturn(false);
        given(enrollmentRepository.saveAndFlush(any(Enrollment.class))).willThrow(DataIntegrityViolationException.class);

        // when & then
        assertThatThrownBy(() -> enrollmentService.createEnrollment(userId, courseId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 신청한 강의는 재신청할 수 없습니다.");
    }

    @Test
    @DisplayName("이미 신청한 강의에 다시 신청하면 예외가 발생한다.")
    void createEnrollmentWithAlreadyEnrolled() {
        // given
        Long userId = 1L;
        Long courseId = 1L;

        User classmate = User.create("수강생01");
        User creator = User.create("강사01");

        ReflectionTestUtils.setField(creator, "id", 2L);

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

        given(userRepository.findById(userId)).willReturn(Optional.of(classmate));
        given(courseRepository.findById(courseId)).willReturn(Optional.of(course));
        given(enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> enrollmentService.createEnrollment(userId, courseId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 신청한 강의는 재신청할 수 없습니다.");

        then(enrollmentRepository).should(never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("수강 신청을 확정할 수 있다.")
    void confirmEnrollment() {
        // given
        Long userId = 1L;
        Long enrollmentId = 1L;

        User classmate = User.create("수강생01");
        User creator = User.create("강사01");

        ReflectionTestUtils.setField(classmate, "id", userId);
        ReflectionTestUtils.setField(creator, "id", 2L);

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

        Enrollment enrollment = Enrollment.create(classmate, course);
        ReflectionTestUtils.setField(enrollment, "id", enrollmentId);

        given(enrollmentRepository.findByIdWithPessimisticLock(enrollmentId)).willReturn(Optional.of(enrollment));
        given(courseRepository.findByIdWithPessimisticLock(course.getId())).willReturn(Optional.of(course));

        // when
        ConfirmEnrollmentResponse response = enrollmentService.confirmEnrollment(userId, enrollmentId);

        // then
        assertThat(response.getEnrollmentId()).isNotNull();
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("수강 신청을 취소할 수 있다.")
    void cancelEnrollment() {
        // given
        Long userId = 1L;
        Long enrollmentId = 1L;

        User classmate = User.create("수강생01");
        User creator = User.create("강사01");

        ReflectionTestUtils.setField(classmate, "id", userId);
        ReflectionTestUtils.setField(creator, "id", 2L);

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

        Enrollment enrollment = Enrollment.create(classmate, course);
        ReflectionTestUtils.setField(enrollment, "id", enrollmentId);

        enrollment.confirm();

        given(enrollmentRepository.findByIdWithPessimisticLock(enrollmentId)).willReturn(Optional.of(enrollment));
        given(courseRepository.findByIdWithPessimisticLock(course.getId())).willReturn(Optional.of(course));

        // when
        CancelEnrollmentResponse response = enrollmentService.cancelEnrollment(userId, enrollmentId);

        // then
        assertThat(response.getEnrollmentId()).isNotNull();
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
    }
}
