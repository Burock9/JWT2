package com.burock.jwt_2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.burock.jwt_2.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    long countByStockLessThanEqual(int stock);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);
}
