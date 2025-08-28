package com.burock.jwt_2.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotBlank(message = "Teslimat adresi boş olamaz")
    private String shippingAddress;

    private String notes;

}
