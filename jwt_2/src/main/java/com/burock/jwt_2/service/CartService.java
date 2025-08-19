package com.burock.jwt_2.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.burock.jwt_2.dto.AddToCartRequest;
import com.burock.jwt_2.dto.CartItemResponse;
import com.burock.jwt_2.dto.CartResponse;
import com.burock.jwt_2.model.Cart;
import com.burock.jwt_2.model.CartItem;
import com.burock.jwt_2.model.Product;
import com.burock.jwt_2.model.User;
import com.burock.jwt_2.repository.CartItemRepository;
import com.burock.jwt_2.repository.CartRepository;
import com.burock.jwt_2.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public void addToCart(User user, AddToCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı."));

        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Ürünün stoğu bitmiştir.");
        }

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(CartItem.builder().cart(cart).product(product).quantity(0).build());
        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());

        cartItemRepository.save(cartItem);
    }

    public CartResponse getCart(User user) {
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Sepet Bulunumadı."));

        List<CartItemResponse> items = cartItemRepository.findByCart(cart).stream()
                .map(ci -> new CartItemResponse(ci.getProduct().getId(), ci.getProduct().getName(), ci.getQuantity(),
                        ci.getProduct().getPrice() * ci.getQuantity())).collect(Collectors.toList());
        double totalPrice = items.stream().mapToDouble(CartItemResponse::getPrice).sum();

        return new CartResponse(items, totalPrice);
    }

    public void removeFromCart(User user, Long productId){
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Sepet Bulunamadı."));

        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Ürün Bulunamadı."));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product).orElseThrow(() -> new RuntimeException("Ürün Sepette Değil."));

        cartItemRepository.delete(cartItem);
    }
}
