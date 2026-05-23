package com.example.classenrollmentsystem.domain.course.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CreateCourseRequest {
    @NotBlank(message = "강의 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "강의 설명은 필수입니다.")
    private String description;

    @NotNull(message = "강의 가격은 필수입니다.")
    @Min(value = 0, message = "강의 가격은 0원 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "강의 정원은 필수입니다.")
    @Min(value = 1, message = "강의 정원은 1명 이상이어야 합니다.")
    private Integer capacity;

    @NotNull(message = "강의 시작일은 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "강의 종료일은 필수입니다.")
    private LocalDate endDate;
}
