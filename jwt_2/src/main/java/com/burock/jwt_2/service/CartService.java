package com.burock.jwt_2.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.burock.jwt_2.dto.AddToCartRequest;
import com.burock.jwt_2.dto.CartItemResponse;
import com.burock.jwt_2.dto.CartResponse;
import com.burock.jwt_2.model.Cart;
import com.burock.jwt_2.model.CartLine;
import com.burock.jwt_2.model.Product;
import com.burock.jwt_2.model.User;
import com.burock.jwt_2.repository.CartItemRepository;
import com.burock.jwt_2.repository.CartRepository;
import com.burock.jwt_2.repository.ProductRepository;
import com.burock.jwt_2.search.model.CartIndex;
import com.burock.jwt_2.search.service.CartSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartSearchService cartSearchService;

    // GET işlemleri Elasticsearch ile
    
    public Optional<CartIndex> getCartByUserId(Long userId) {
        log.info("Kullanıcı sepeti Elasticsearch'ten getiriliyor: {}", userId);
        return cartSearchService.getByUserId(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<CartIndex> getAllCarts(Pageable pageable) {
        log.info("Tüm sepetler Elasticsearch'ten getiriliyor...");
        return cartSearchService.getAll(pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<CartIndex> searchCartsByUserName(String userName, Pageable pageable) {
        log.info("Sepetler kullanıcı adına göre aranıyor: '{}'", userName);
        return cartSearchService.searchByUserName(userName, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<CartIndex> findCartsWithProduct(Long productId, Pageable pageable) {
        log.info("Ürün içeren sepetler bulunuyor: {}", productId);
        return cartSearchService.findCartsWithProduct(productId, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<CartIndex> findCartsByPriceRange(double minPrice, double maxPrice, Pageable pageable) {
        log.info("Fiyat aralığındaki sepetler bulunuyor: {}-{}", minPrice, maxPrice);
        return cartSearchService.findCartsByPriceRange(minPrice, maxPrice, pageable);
    }

    // CUD işlemleri

    public void addToCart(User user, AddToCartRequest request) {
        log.info("Sepete ürün ekleniyor kullanıcı: {}, ürün: {}", user.getUsername(), request.getProductId());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı."));

        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Ürünün stoğu bitmiştir.");
        }

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));

        CartLine cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(CartLine.builder().cart(cart).product(product).quantity(0).build());
        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());

        cartItemRepository.save(cartItem);

        // Elasticsearch'i güncelle
        try {
            Cart updatedCart = cartRepository.findByUser(user).get();
            cartSearchService.indexCart(updatedCart);
            log.info("Sepet Elasticsearch'te başarıyla güncellendi");
        } catch (Exception e) {
            log.error("Sepet Elasticsearch'te güncellenemedi: {}", e.getMessage());
        }
    }

    public CartResponse getCart(User user) {
        log.info("Kullanıcı sepeti getiriliyor: {}", user.getUsername());

        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Sepet Bulunamadı."));

        List<CartItemResponse> items = cartItemRepository.findByCart(cart).stream()
                .map(ci -> new CartItemResponse(ci.getProduct().getId(), ci.getProduct().getName(), ci.getQuantity(),
                        ci.getProduct().getPrice() * ci.getQuantity()))
                .collect(Collectors.toList());
        double totalPrice = items.stream().mapToDouble(CartItemResponse::getPrice).sum();

        return new CartResponse(items, totalPrice);
    }

    public void removeFromCart(User user, Long productId) {
        log.info("Sepetten ürün siliniyor kullanıcı: {}, ürün: {}", user.getUsername(), productId);

        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Sepet Bulunamadı."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Ürün Bulunamadı."));

        CartLine cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new RuntimeException("Ürün Sepette Değil."));

        cartItemRepository.delete(cartItem);

        try {
            Optional<Cart> updatedCart = cartRepository.findByUser(user);
            if (updatedCart.isPresent()) {
                cartSearchService.indexCart(updatedCart.get());
                log.info("Sepet Elasticsearch'te başarıyla güncellendi");
            } else {
                cartSearchService.deleteByUserId(user.getId());
                log.info("Boş sepet Elasticsearch'ten silindi");
            }
        } catch (Exception e) {
            log.error("Sepet Elasticsearch'te güncellenemedi: {}", e.getMessage());
        }
    }
}
