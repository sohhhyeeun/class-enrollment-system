package com.example.classenrollmentsystem.domain.course.controller;

import com.example.classenrollmentsystem.common.ApiResponse;
import com.example.classenrollmentsystem.domain.course.dto.request.CreateCourseRequest;
import com.example.classenrollmentsystem.domain.course.dto.response.CreateCourseResponse;
import com.example.classenrollmentsystem.domain.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                .body(ApiResponse.success("강의 등록이 완료되었습니다.", response));
    }
}
