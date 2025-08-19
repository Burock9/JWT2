package com.burock.jwt_2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.burock.jwt_2.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
