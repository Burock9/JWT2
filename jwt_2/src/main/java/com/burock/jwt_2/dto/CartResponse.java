package com.burock.jwt_2.dto;

import java.util.List;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class CartResponse {
    private List<CartItemResponse> items;
    private double totalPrice;
}
