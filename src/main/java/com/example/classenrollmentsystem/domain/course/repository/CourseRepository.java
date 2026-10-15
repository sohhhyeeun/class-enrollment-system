package com.example.classenrollmentsystem.domain.course.repository;

import com.example.classenrollmentsystem.domain.course.entity.Course;
import com.example.classenrollmentsystem.domain.course.entity.CourseStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query(
            value = "SELECT c FROM Course c JOIN FETCH c.user WHERE (:status IS NULL OR c.status = :status) ORDER BY c.id DESC",
            countQuery = "SELECT COUNT(c) FROM Course c WHERE (:status IS NULL OR c.status = :status)"
    )
    Page<Course> findCoursesWithUser(@Param("status") CourseStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findByIdWithPessimisticLock(@Param("id") Long id);
}
