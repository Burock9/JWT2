package com.burock.jwt_2.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.burock.jwt_2.dto.AddToCartRequest;
import com.burock.jwt_2.dto.ApiResponse;
import com.burock.jwt_2.dto.CartResponse;
import com.burock.jwt_2.model.User;
import com.burock.jwt_2.search.model.CartIndex;
import com.burock.jwt_2.service.CartService;
import com.burock.jwt_2.service.MessageService;
import com.burock.jwt_2.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final MessageService messageService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addToCart(@RequestBody AddToCartRequest request, Principal principal) {
        try {
            User user = userService.getByUsernameSecured(principal.getName());
            cartService.addToCart(user, request);
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("cart.item.added"),
                    null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Principal principal) {
        User user = userService.getByUsernameSecured(principal.getName());
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @GetMapping("/elasticsearch")
    public ResponseEntity<Optional<CartIndex>> getCartFromElasticsearch(Principal principal) {
        User user = userService.getByUsernameSecured(principal.getName());
        return ResponseEntity.ok(cartService.getCartByUserId(user.getId()));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<String>> removeFromCart(@PathVariable Long productId, Principal principal) {
        try {
            User user = userService.getByUsernameSecured(principal.getName());
            cartService.removeFromCart(user, productId);
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("cart.item.removed"),
                    null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @GetMapping("/my-cart/analytics")
    public ResponseEntity<CartIndex> getMyCartAnalytics(Principal principal) {
        User user = userService.getByUsernameSecured(principal.getName());
        Optional<CartIndex> cart = cartService.getCartByUserId(user.getId());
        if (cart.isPresent()) {
            return ResponseEntity.ok(cart.get());
        } else {
            throw new RuntimeException(messageService.getMessage("cart.not.found"));
        }
    }

    // Admin işlemleri - Sadece analiz ve raporlama amaçlı

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<Page<CartIndex>> getAllCarts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(cartService.getAllCarts(PageRequest.of(page, size)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/search")
    public ResponseEntity<Page<CartIndex>> searchCartsByUserName(
            @RequestParam String userName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(cartService.searchCartsByUserName(userName, PageRequest.of(page, size)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/product/{productId}")
    public ResponseEntity<Page<CartIndex>> findCartsWithProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(cartService.findCartsWithProduct(productId, PageRequest.of(page, size)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/price-range")
    public ResponseEntity<Page<CartIndex>> findCartsByPriceRange(
            @RequestParam double minPrice,
            @RequestParam double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(cartService.findCartsByPriceRange(minPrice, maxPrice, PageRequest.of(page, size)));
    }
}
