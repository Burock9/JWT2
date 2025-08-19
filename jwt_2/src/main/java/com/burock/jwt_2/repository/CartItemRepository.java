package com.burock.jwt_2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.burock.jwt_2.model.Cart;
import com.burock.jwt_2.model.CartItem;
import com.burock.jwt_2.model.Product;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    List<CartItem> findByCart(Cart cart);
}
