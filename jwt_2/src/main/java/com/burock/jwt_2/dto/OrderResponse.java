package com.burock.jwt_2.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.burock.jwt_2.model.OrderStatus;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private List<OrderItemResponse> orderItems;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String statusText; 
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private String shippingAddress;
    private String notes;
}
