package com.example.classenrollmentsystem.domain.enrollment.controller;

import com.example.classenrollmentsystem.common.ApiResponse;
import com.example.classenrollmentsystem.domain.enrollment.dto.response.CancelEnrollmentResponse;
import com.example.classenrollmentsystem.domain.enrollment.dto.response.ConfirmEnrollmentResponse;
import com.example.classenrollmentsystem.domain.enrollment.dto.response.CreateEnrollmentResponse;
import com.example.classenrollmentsystem.domain.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}/enrollments")
    public ResponseEntity<ApiResponse<CreateEnrollmentResponse>> createEnrollment(@RequestHeader("X-USER-ID") Long userId, @PathVariable("courseId") Long courseId) {
        CreateEnrollmentResponse response = enrollmentService.createEnrollment(userId, courseId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("수강 신청이 되었습니다.", response));
    }

    @PatchMapping("/enrollments/{enrollmentId}/confirm")
    public ResponseEntity<ApiResponse<ConfirmEnrollmentResponse>> confirmEnrollment(@RequestHeader("X-USER-ID") Long userId, @PathVariable("enrollmentId") Long enrollmentId) {
        ConfirmEnrollmentResponse response = enrollmentService.confirmEnrollment(userId, enrollmentId);

        return ResponseEntity
                .ok(ApiResponse.success("수강 확정이 되었습니다.", response));
    }

    @PatchMapping("/enrollments/{enrollmentId}/cancel")
    public ResponseEntity<ApiResponse<CancelEnrollmentResponse>> cancelEnrollment(@RequestHeader("X-USER-ID") Long userId, @PathVariable("enrollmentId") Long enrollmentId) {
        CancelEnrollmentResponse response = enrollmentService.cancelEnrollment(userId, enrollmentId);

        return ResponseEntity
                .ok(ApiResponse.success("수강 취소가 되었습니다.", response));
    }
}
