package com.example.classenrollmentsystem.domain.course.dto.response;

import com.example.classenrollmentsystem.domain.course.entity.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateCourseStatusResponse {
    private Long courseId;
    private CourseStatus status;
}
