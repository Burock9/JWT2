package com.burock.jwt_2.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class CartItemResponse {
    private Long productId;
    private String productName;
    private int quantity;
    private double price;
}
