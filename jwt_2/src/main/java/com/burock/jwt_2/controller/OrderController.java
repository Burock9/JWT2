package com.burock.jwt_2.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.burock.jwt_2.dto.ApiResponse;
import com.burock.jwt_2.dto.CreateOrderRequest;
import com.burock.jwt_2.dto.OrderResponse;
import com.burock.jwt_2.model.OrderStatus;
import com.burock.jwt_2.service.MessageService;
import com.burock.jwt_2.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MessageService messageService;

    // Kullanıcı İşlemleri

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request,
            Principal principal) {
        try {
            OrderResponse order = orderService.createOrder(request, principal.getName());
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("order.created"),
                    order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(Principal principal) {
        try {
            List<OrderResponse> orders = orderService.getUserOrders(principal.getName());
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("success"),
                    orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @GetMapping("/my-orders/paged")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrdersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        try {
            Page<OrderResponse> orders = orderService.getUserOrdersPaged(principal.getName(),
                    PageRequest.of(page, size));
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("success"),
                    orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @GetMapping("/my-orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getMyOrderById(@PathVariable Long orderId, Principal principal) {
        try {
            OrderResponse order = orderService.getOrderById(orderId, principal.getName());
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("success"),
                    order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("order.not.found"),
                    null));
        }
    }

    @GetMapping("/my-orders/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrdersByStatus(@PathVariable OrderStatus status,
            Principal principal) {
        try {
            List<OrderResponse> orders = orderService.getUserOrdersByStatus(principal.getName(), status);
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("success"),
                    orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelMyOrder(@PathVariable Long orderId, Principal principal) {
        try {
            OrderResponse order = orderService.cancelOrder(orderId, principal.getName());
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("order.cancelled"),
                    order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("order.cannot.cancel"),
                    null));
        }
    }

    @GetMapping("my-orders/count")
    public ResponseEntity<ApiResponse<Long>> getMyOrderCount(Principal principal) {
        try {
            Long count = orderService.getUserOrderCount(principal.getName());
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("success"),
                    count));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @GetMapping("/my-orders/total-spending")
    public ResponseEntity<ApiResponse<BigDecimal>> getMyTotalSpending(Principal principal) {
        try {
            BigDecimal totalSpending = orderService.getUserTotalSpending(principal.getName());
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("success"),
                    totalSpending));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @GetMapping("/my-orders/{orderId}/summary")
    public ResponseEntity<ApiResponse<String>> getMyOrderSummary(@PathVariable Long orderId, Principal principal) {
        try {
            orderService.getOrderById(orderId, principal.getName());
            String summary = orderService.getOrderSummary(orderId);
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("success"),
                    summary));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("order.not.found"),
                    null));
        }
    }

    @GetMapping("/my-orders/{orderId}/delivery-time")
    public ResponseEntity<ApiResponse<String>> getMyOrderDeliveryTime(@PathVariable Long orderId, Principal principal) {
        try {
            orderService.getOrderById(orderId, principal.getName());
            String deliveryTime = orderService.calculateDeliveryTime(orderId);
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("success"),
                    deliveryTime));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("order.not.found"),
                    null));
        }
    }

    // Genel Arama İşlemleri

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> searchOrders(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<OrderResponse> orders = orderService.searchOrdersInElasticsearch(query,
                    PageRequest.of(page, size));
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("success"),
                    orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("search.no.results"),
                    null));
        }
    }

    @GetMapping("/search/status")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> searchOrdersByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<OrderResponse> orders = orderService.searchOrdersByStatusInElasticsearch(status,
                    PageRequest.of(page, size));
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("success"),
                    orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("search.no.results"),
                    null));
        }
    }

    @GetMapping("/order-number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> gerOrderByOrderNumber(@PathVariable String orderNumber) {
        try {
            Optional<OrderResponse> order = orderService.getOrderByOrderNumber(orderNumber);
            if (order.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(
                        messageService.getMessage("success"),
                        order.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("order.not.found"),
                    null));
        }
    }

    // Admin İşlemleri

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<OrderResponse> orders = orderService.getAllOrders(PageRequest.of(page, size));
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("success"),
                    orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<OrderResponse> orders = orderService.getOrdersByStatus(status, PageRequest.of(page, size));
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("success"),
                    orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{orderId}/{status}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId, @RequestParam OrderStatus status) {
        try {
            OrderResponse order = orderService.updateOrderStatus((orderId), status);
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("order.updated"),
                    order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("order.not.found"),
                    null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/cancel/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrderByAdmin(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        try {
            OrderResponse order = orderService.cancelOrderByAdmin(orderId, reason);
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("order.cancelled.by.admin"),
                    order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("order.cannot.cancel"),
                    null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/between-dates")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<OrderResponse> orders = orderService.getOrdersBetweenDates(startDate, endDate);
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("success"),
                    orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByIdAdmin(@PathVariable Long orderId) {
        try {
            Optional<OrderResponse> order = orderService.getOrderByOrderNumber(orderService
                    .getAllOrders(PageRequest.of(0, Integer.MAX_VALUE)).stream().filter(o -> o.getId().equals(orderId))
                    .findFirst().map(OrderResponse::getOrderNumber).orElse(""));
            if (order.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(
                        messageService.getMessage("success"),
                        order.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("order.not.found"),
                    null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{orderId}/summary")
    public ResponseEntity<ApiResponse<String>> getOrderSummaryAdmin(@PathVariable Long orderId) {
        try {
            String summary = orderService.getOrderSummary(orderId);
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("success"),
                    summary));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("order.not.found"),
                    null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{orderId}/delivery-time")
    public ResponseEntity<ApiResponse<String>> getOrderDeliveryTimeAdmin(@PathVariable Long orderId) {
        try {
            String deliveryTime = orderService.calculateDeliveryTime(orderId);
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("success"),
                    deliveryTime));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("order.not.found"),
                    null));
        }
    }

}
