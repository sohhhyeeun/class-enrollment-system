package com.example.classenrollmentsystem.user.repository;

import com.example.classenrollmentsystem.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
