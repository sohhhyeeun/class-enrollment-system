package com.example.classenrollmentsystem.domain.course.repository;

import com.example.classenrollmentsystem.domain.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
