package com.example.classenrollmentsystem.domain.course.dto.response;

import com.example.classenrollmentsystem.domain.course.entity.Course;
import com.example.classenrollmentsystem.domain.course.entity.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class CourseDetailResponse {
    private Long id;
    private String title;
    private String description;
    private Integer price;
    private Integer capacity;
    private Integer currentEnrollmentCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private CourseStatus status;
    private String creatorName;

    public static CourseDetailResponse from(Course course) {
        return new CourseDetailResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getPrice(),
                course.getCapacity(),
                course.getCurrentEnrollmentCount(),
                course.getStartDate(),
                course.getEndDate(),
                course.getStatus(),
                course.getUser().getName()
        );
    }
}
