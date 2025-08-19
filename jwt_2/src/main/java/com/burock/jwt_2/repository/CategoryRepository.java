package com.burock.jwt_2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.burock.jwt_2.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
}
