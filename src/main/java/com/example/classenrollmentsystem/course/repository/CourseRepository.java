package com.example.classenrollmentsystem.course.repository;

import com.example.classenrollmentsystem.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
