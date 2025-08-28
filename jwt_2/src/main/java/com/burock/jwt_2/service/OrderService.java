package com.burock.jwt_2.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.burock.jwt_2.dto.CreateOrderRequest;
import com.burock.jwt_2.dto.OrderItemResponse;
import com.burock.jwt_2.dto.OrderResponse;
import com.burock.jwt_2.model.Cart;
import com.burock.jwt_2.model.Order;
import com.burock.jwt_2.model.OrderItem;
import com.burock.jwt_2.model.OrderStatus;
import com.burock.jwt_2.model.Product;
import com.burock.jwt_2.model.User;
import com.burock.jwt_2.repository.CartRepository;
import com.burock.jwt_2.repository.OrderRepository;
import com.burock.jwt_2.repository.ProductRepository;
import com.burock.jwt_2.repository.UserRepository;
import com.burock.jwt_2.search.service.OrderSearchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderSearchService orderSearchService;
    private final MessageService messageService;

    private String generateOrderNumber() {
        return "SIP-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private OrderResponse convertToOrderResponse(Order order) {
        List<OrderItemResponse> orderItems = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder().id(item.getId()).productId(item.getProduct().getId())
                        .productName(item.getProduct().getName()).quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice()).totalPrice(item.getTotalPrice()).build())
                .collect(Collectors.toList());

        return OrderResponse.builder().id(order.getId()).orderNumber(order.getOrderNumber())
                .orderItems(orderItems).totalAmount(order.getTotalAmount()).status(order.getStatus())
                .orderDate(order.getOrderDate()).deliveryDate(order.getDeliveryDate())
                .shippingAddress(order.getShippingAddress()).notes(order.getNotes()).build();
    }

    public OrderResponse createOrder(CreateOrderRequest request, String username) {
        log.info("{} Kullanıcısı için sipariş oluşturuluyor", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("auth.user.not.found")));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("cart.not.found")));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException(messageService.getMessage("order.empty.cart"));
        }

        Order order = Order.builder().orderNumber(generateOrderNumber()).user(user).totalAmount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING).orderDate(LocalDateTime.now())
                .shippingAddress(request.getShippingAddress()).notes(request.getNotes()).build();

        List<OrderItem> orderItems = cart.getItems().stream().map(cartLine -> {
            Product product = cartLine.getProduct();
            if (product.getStock() < cartLine.getQuantity()) {
                throw new RuntimeException(
                        messageService.getMessage("order.insufficient.stock", new Object[] { product.getName() }));
            }

            product.setStock(product.getStock() - cartLine.getQuantity());
            productRepository.save(product);

            return OrderItem.builder().order(order).product(product).quantity(cartLine.getQuantity())
                    .unitPrice(BigDecimal.valueOf(product.getPrice())).build();

        }).collect(Collectors.toList());

        order.setOrderItems(orderItems);

        BigDecimal totalAmount = orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        log.info("Sipariş başarıyla oluşturuldu: {}", savedOrder.getOrderNumber());

        orderSearchService.indexOrder(savedOrder);

        cart.getItems().clear();
        cartRepository.save(cart);

        return convertToOrderResponse(savedOrder);
    }

    public List<OrderResponse> getUserOrders(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("auth.user.not.found")));

        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
        return orders.stream().map(this::convertToOrderResponse).collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long orderId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("auth.user.not.found")));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("order.not.found")));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(messageService.getMessage("order.access.denied"));
        }

        return convertToOrderResponse(order);
    }

    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("order.not.found")));

        order.setStatus(status);

        if (status == OrderStatus.DELIVERED) {
            order.setDeliveryDate(LocalDateTime.now());
        }

        Order savedOrder = orderRepository.save(order);

        orderSearchService.indexOrder(savedOrder);

        return convertToOrderResponse(savedOrder);
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        log.info("Tüm siparişler getiriliyor...");
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::convertToOrderResponse);
    }

    public Optional<OrderResponse> getOrderByOrderNumber(String orderNumber) {
        log.info("Sipariş numarasına göre sipariş getiriliyor: {}", orderNumber);
        return orderRepository.findByOrderNumber(orderNumber)
                .map(this::convertToOrderResponse);
    }

    public Long getUserOrderCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("auth.user.not.found")));
        return orderRepository.countOrdersByUser(user);
    }

    public Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        log.info("Duruma göre siparişler getiriliyor: {}", status);
        Page<Order> orders = orderRepository.findByStatusOrderByOrderDateDesc(status, pageable);
        return orders.map(this::convertToOrderResponse);
    }

    public List<OrderResponse> getUserOrdersByStatus(String username, OrderStatus status) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("auth.user.not.found")));

        List<Order> orders = orderRepository.findByUserAndStatus(user, status);
        return orders.stream().map(this::convertToOrderResponse).collect(Collectors.toList());
    }

    public Page<OrderResponse> getUserOrdersPaged(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("auth.user.not.found")));

        Page<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user, pageable);
        return orders.map(this::convertToOrderResponse);
    }

    public List<OrderResponse> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Tarihler arası siparişler getiriliyor: {} - {}", startDate, endDate);
        List<Order> orders = orderRepository.findOrdersBetweenDated(startDate, endDate);
        return orders.stream().map(this::convertToOrderResponse).collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, String username) {
        log.info("Kullanıcı : {} tarafından sipariş iptal ediliyor: {}", orderId, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("auth.user.not.found")));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("order.not.found")));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(messageService.getMessage("order.access.denied"));
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException(messageService.getMessage("order.cannot.cancel"));
        }

        order.getOrderItems().forEach(orderItem -> {
            Product product = orderItem.getProduct();
            product.setStock(product.getStock() + orderItem.getQuantity());
            productRepository.save(product);
        });

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        orderSearchService.indexOrder(savedOrder);

        log.info("Sipariş başarıyla iptal edildi: {}", order.getOrderNumber());
        return convertToOrderResponse(savedOrder);
    }

    @Transactional
    public OrderResponse cancelOrderByAdmin(Long orderId, String reason) {
        log.info("Yönetici siparişi iptal etti: {} İptal sebebi: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("order.not.found")));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException(messageService.getMessage("order.delivered.cannot.cancel"));
        }

        if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED) {
            order.getOrderItems().forEach(orderItem -> {
                Product product = orderItem.getProduct();
                product.setStock(product.getStock() + orderItem.getQuantity());
                productRepository.save(product);
            });
        }

        order.setStatus(OrderStatus.CANCELLED);
        if (reason != null && !reason.trim().isEmpty()) {
            order.setNotes(order.getNotes() + " [İptal sebebi: " + reason + "]");
        }

        Order savedOrder = orderRepository.save(order);

        orderSearchService.indexOrder(savedOrder);

        log.info("Yönetici siparişi iptal etti: {}", order.getOrderNumber());
        return convertToOrderResponse(savedOrder);
    }

    public String getOrderSummary(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("order.not.found")));

        int totalItems = order.getOrderItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        return messageService.getMessage("order.summary",
                new Object[] { order.getOrderNumber(), totalItems, order.getTotalAmount(), order.getStatus() });
    }

    public BigDecimal getUserTotalSpending(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("auth.user.not.found")));

        List<Order> completedOrders = orderRepository.findByUserAndStatus(user, OrderStatus.DELIVERED);

        return completedOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String calculateDeliveryTime(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("order.not.found")));

        if (order.getDeliveryDate() == null) {
            return messageService.getMessage("order.not.delivered");
        }

        LocalDateTime orderDate = order.getOrderDate();
        LocalDateTime deliveryDate = order.getDeliveryDate();

        long days = java.time.Duration.between(orderDate, deliveryDate).toDays();
        long hours = java.time.Duration.between(orderDate, deliveryDate).toHours() % 24;

        return messageService.getMessage("order.delivery.time", new Object[] { days, hours });
    }

    public Page<OrderResponse> searchOrdersInElasticsearch(String query, Pageable pageable) {
        log.info("Siparişler Elasticsearch ile sırayla getiriliyor: {}", query);
        return orderSearchService.searchOrders(query, pageable)
                .map(orderIndex -> {
                    return OrderResponse.builder()
                            .id(Long.valueOf(orderIndex.getId()))
                            .orderNumber(orderIndex.getOrderNumber())
                            .totalAmount(orderIndex.getTotalAmount())
                            .status(OrderStatus.valueOf(orderIndex.getStatus()))
                            .orderDate(orderIndex.getOrderDate())
                            .deliveryDate(orderIndex.getDeliveryDate())
                            .shippingAddress(orderIndex.getShippingAddress())
                            .notes(orderIndex.getNotes())
                            .build();
                });
    }

    public Page<OrderResponse> searchOrdersByStatusInElasticsearch(String status, Pageable pageable) {
        log.info("Siparişler Elasticsearch ile durumuna göre getiriliyor: {}", status);
        return orderSearchService.findByStatus(status, pageable)
                .map(orderIndex -> {
                    return OrderResponse.builder()
                            .id(Long.valueOf(orderIndex.getId()))
                            .orderNumber(orderIndex.getOrderNumber())
                            .totalAmount(orderIndex.getTotalAmount())
                            .status(OrderStatus.valueOf(orderIndex.getStatus()))
                            .orderDate(orderIndex.getOrderDate())
                            .deliveryDate(orderIndex.getDeliveryDate())
                            .shippingAddress(orderIndex.getShippingAddress())
                            .notes(orderIndex.getNotes())
                            .build();
                });
    }

}
