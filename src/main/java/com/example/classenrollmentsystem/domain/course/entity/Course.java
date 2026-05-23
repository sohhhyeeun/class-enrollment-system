package com.example.classenrollmentsystem.domain.course.entity;

import com.example.classenrollmentsystem.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "courses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer currentEnrollmentCount;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Course(String title, String description, Integer price, Integer capacity, LocalDate startDate, LocalDate endDate, User user) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.capacity = capacity;
        this.currentEnrollmentCount = 0;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = CourseStatus.DRAFT;
        this.user = user;
    }

    public static Course create(String title, String description, Integer price, Integer capacity, LocalDate startDate, LocalDate endDate, User user) {
        return new Course(title, description, price, capacity, startDate, endDate, user);
    }

    public void updateStatus(CourseStatus status) {
        this.status = status;
    }
}
