package com.example.classenrollmentsystem.domain.enrollment.controller;

import com.example.classenrollmentsystem.common.ApiResponse;
import com.example.classenrollmentsystem.domain.enrollment.dto.response.CreateEnrollmentResponse;
import com.example.classenrollmentsystem.domain.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/courses")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @PostMapping("/{courseId}/enrollments")
    public ResponseEntity<ApiResponse<CreateEnrollmentResponse>> createEnrollment(@RequestHeader("X-USER-ID") Long userId, @PathVariable("courseId") Long courseId) {
        CreateEnrollmentResponse response = enrollmentService.createEnrollment(userId, courseId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("수강 신청이 되었습니다.", response));
    }
}
