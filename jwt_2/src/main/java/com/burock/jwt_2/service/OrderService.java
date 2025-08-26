package com.burock.jwt_2.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.burock.jwt_2.dto.CreateOrderRequest;
import com.burock.jwt_2.dto.OrderResponse;
import com.burock.jwt_2.model.Cart;
import com.burock.jwt_2.model.Order;
import com.burock.jwt_2.model.OrderItem;
import com.burock.jwt_2.model.OrderStatus;
import com.burock.jwt_2.model.User;
import com.burock.jwt_2.repository.CartRepository;
import com.burock.jwt_2.repository.OrderRepository;
import com.burock.jwt_2.repository.ProductRepository;
import com.burock.jwt_2.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderResponse createOrder(CreateOrderRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Sepet bulunamadı"));

        if (cart.getCartLines().isEmpty()) {
            throw new RuntimeException("Sepet boş");
        }

        Order order = Order.builder().orderNumber(generateOrderNumber()).user(user).totalAmount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING).orderDate(LocalDateTime.now())
                .shippingAddress(request.getShippingAddress()).notes(request.getNotes()).build;

        List<OrderItem> orderItems = cart.getCartLines().stream().map(cartLine -> {
            Product product = cartLine.getProduct();
            if (product.getStock() < cartLine.getQuantity()) {
                throw new RuntimeException(product.getName() + " ürünü için yetersiz stok");
            }

            product.setStock(product.getStock() - cartLine.getQuantity());
            productRepository.save(product);

            return OrderItem.builder().order(order).product(product).quantity(cartLine.getQuantity())
                    .unitPrice(product.getPrice()).build();

        }).collect(Collectors.toList());

        // burda kaldım
    }
}
