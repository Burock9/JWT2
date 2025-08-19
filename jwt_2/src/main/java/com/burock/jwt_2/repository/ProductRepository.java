package com.burock.jwt_2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.burock.jwt_2.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
