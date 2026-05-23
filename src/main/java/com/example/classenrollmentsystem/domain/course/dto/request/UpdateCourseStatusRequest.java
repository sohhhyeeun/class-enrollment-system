package com.example.classenrollmentsystem.domain.course.dto.request;

import com.example.classenrollmentsystem.domain.course.entity.CourseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateCourseStatusRequest {
    @NotNull(message = "강의 상태는 필수입니다.")
    private CourseStatus status;
}
