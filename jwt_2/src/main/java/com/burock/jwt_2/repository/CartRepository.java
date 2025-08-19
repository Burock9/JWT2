package com.burock.jwt_2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.burock.jwt_2.model.Cart;
import com.burock.jwt_2.model.User;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
