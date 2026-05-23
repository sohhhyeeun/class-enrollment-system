package com.example.classenrollmentsystem.domain.course.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateCourseResponse {
    private Long courseId;
    private String title;
}
