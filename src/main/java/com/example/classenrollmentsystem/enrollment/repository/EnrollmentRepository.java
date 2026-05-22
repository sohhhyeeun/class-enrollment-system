package com.example.classenrollmentsystem.enrollment.repository;

import com.example.classenrollmentsystem.enrollment.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
}
