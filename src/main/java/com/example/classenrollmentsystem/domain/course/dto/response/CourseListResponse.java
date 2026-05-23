package com.example.classenrollmentsystem.domain.course.dto.response;

import com.example.classenrollmentsystem.domain.course.entity.Course;
import com.example.classenrollmentsystem.domain.course.entity.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class CourseListResponse {
    private Long id;
    private String title;
    private Integer price;
    private Integer capacity;
    private LocalDate startDate;
    private LocalDate endDate;
    private CourseStatus status;
    private String creatorName;

    public static CourseListResponse from(Course course) {
        return new CourseListResponse(
                course.getId(),
                course.getTitle(),
                course.getPrice(),
                course.getCapacity(),
                course.getStartDate(),
                course.getEndDate(),
                course.getStatus(),
                course.getUser().getName()
        );
    }
}
