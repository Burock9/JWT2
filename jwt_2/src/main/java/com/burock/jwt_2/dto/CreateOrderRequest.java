package com.burock.jwt_2.dto;

import com.burock.jwt_2.model.Address;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @Valid
    @NotNull
    private Address shippingAddress;

    private String notes;

}
