package com.example.classenrollmentsystem.domain.enrollment.dto.response;

import com.example.classenrollmentsystem.domain.enrollment.entity.Enrollment;
import com.example.classenrollmentsystem.domain.enrollment.entity.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyEnrollmentResponse {
    private Long enrollmentId;
    private Long courseId;
    private String courseTitle;
    private LocalDateTime enrolledAt;
    private EnrollmentStatus enrollmentStatus;

    public static MyEnrollmentResponse from(Enrollment enrollment) {
        return new MyEnrollmentResponse(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getEnrolledAt(),
                enrollment.getStatus()
        );
    }
}
