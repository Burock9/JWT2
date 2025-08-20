package com.burock.jwt_2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.burock.jwt_2.model.Cart;
import com.burock.jwt_2.model.CartLine;
import com.burock.jwt_2.model.Product;

public interface CartItemRepository extends JpaRepository<CartLine, Long> {
    Optional<CartLine> findByCartAndProduct(Cart cart, Product product);

    List<CartLine> findByCart(Cart cart);
}
