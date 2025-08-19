package com.burock.jwt_2.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.burock.jwt_2.dto.AddToCartRequest;
import com.burock.jwt_2.dto.CartResponse;
import com.burock.jwt_2.model.User;
import com.burock.jwt_2.service.CartService;
import com.burock.jwt_2.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @PostMapping("/add")
    public ResponseEntity<String> addtoCart(@RequestBody AddToCartRequest request, Principal principal) {
        User user = userService.getByUsernameSecured(principal.getName());
        cartService.addToCart(user, request);
        return ResponseEntity.ok("Ürün karta eklendi.");
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Principal principal) {
        User user = userService.getByUsernameSecured(principal.getName());
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<String> removeFromCart(@PathVariable Long productId, Principal principal) {
        User user = userService.getByUsernameSecured(principal.getName());
        cartService.removeFromCart(user, productId);
        return ResponseEntity.ok("Ürün Sepetten Kaldırıldı");
    }
}
