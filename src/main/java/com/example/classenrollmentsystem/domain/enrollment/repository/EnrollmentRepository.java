package com.example.classenrollmentsystem.domain.enrollment.repository;

import com.example.classenrollmentsystem.domain.enrollment.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
