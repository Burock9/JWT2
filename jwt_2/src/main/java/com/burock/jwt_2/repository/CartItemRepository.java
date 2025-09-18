package com.burock.jwt_2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.burock.jwt_2.model.Cart;
import com.burock.jwt_2.model.CartLine;
import com.burock.jwt_2.model.Product;

public interface CartItemRepository extends JpaRepository<CartLine, Long> {
    Optional<CartLine> findByCartAndProduct(Cart cart, Product product);

    List<CartLine> findByCart(Cart cart);

    // Sepete ekleme sırasına göre sıralı liste (ID artan sırada)
    @Query("SELECT cl FROM CartLine cl WHERE cl.cart = ?1 ORDER BY cl.id ASC")
    List<CartLine> findByCartOrderById(Cart cart);

    void deleteAllByCart(Cart cart);
}
