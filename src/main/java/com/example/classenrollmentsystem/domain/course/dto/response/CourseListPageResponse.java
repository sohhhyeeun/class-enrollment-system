package com.example.classenrollmentsystem.domain.course.dto.response;

import com.example.classenrollmentsystem.domain.course.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class CourseListPageResponse {
    private List<CourseListResponse> courses;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private Boolean hasNext;

    public static CourseListPageResponse from(Page<Course> coursePage) {
        List<CourseListResponse> courses = coursePage.getContent().stream()
                .map(CourseListResponse::from)
                .toList();

        return new CourseListPageResponse(
                courses,
                coursePage.getNumber(),
                coursePage.getSize(),
                coursePage.getTotalElements(),
                coursePage.getTotalPages(),
                coursePage.hasNext()
        );
    }
}
