package com.example.classenrollmentsystem.domain.course.controller;

import com.example.classenrollmentsystem.common.ApiResponse;
import com.example.classenrollmentsystem.domain.course.dto.request.CreateCourseRequest;
import com.example.classenrollmentsystem.domain.course.dto.request.UpdateCourseStatusRequest;
import com.example.classenrollmentsystem.domain.course.dto.response.CourseDetailResponse;
import com.example.classenrollmentsystem.domain.course.dto.response.CourseListResponse;
import com.example.classenrollmentsystem.domain.course.dto.response.CreateCourseResponse;
import com.example.classenrollmentsystem.domain.course.dto.response.UpdateCourseStatusResponse;
import com.example.classenrollmentsystem.domain.course.entity.CourseStatus;
import com.example.classenrollmentsystem.domain.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/courses")
public class CourseController {
    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateCourseResponse>> createCourse(@RequestHeader("X-USER-ID") Long userId, @Valid @RequestBody CreateCourseRequest request) {
        CreateCourseResponse response = courseService.createCourse(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("강의가 등록되었습니다.", response));
    }

    @PatchMapping("/{courseId}/status")
    public ResponseEntity<ApiResponse<UpdateCourseStatusResponse>> updateCourseStatus(@RequestHeader("X-USER-ID") Long userId, @PathVariable("courseId") Long courseId, @Valid @RequestBody UpdateCourseStatusRequest request) {
        UpdateCourseStatusResponse response = courseService.updateCourseStatus(userId, courseId, request);

        return ResponseEntity
                .ok(ApiResponse.success("강의 상태가 변경되었습니다.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseListResponse>>> getCourses(@RequestParam(name = "status", required = false) CourseStatus status) {
        List<CourseListResponse> response = courseService.getCourses(status);

        return ResponseEntity
                .ok(ApiResponse.success("강의 목록이 조회되었습니다.", response));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseDetail(@PathVariable("courseId") Long courseId) {
        CourseDetailResponse response = courseService.getCourseDetail(courseId);

        return ResponseEntity
                .ok(ApiResponse.success("강의 상세가 조회되었습니다.", response));
    }
}
