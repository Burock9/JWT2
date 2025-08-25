package com.burock.jwt_2.search.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.burock.jwt_2.model.Cart;
import com.burock.jwt_2.model.CartLine;
import com.burock.jwt_2.search.model.CartIndex;
import com.burock.jwt_2.search.repository.CartSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartSearchService {

    private final CartSearchRepository cartSearchRepository;

    public void indexCart(Cart cart) {
        log.info("Sepet Elasticsearch'e indeksleniyor kullanıcı: {}", cart.getUser().getUsername());

        // Cart item'larını CartItemIndex'e dönüştür
        List<CartIndex.CartItemIndex> cartItems = cart.getItems().stream()
                .map(this::convertToCartItemIndex)
                .collect(Collectors.toList());

        // Toplam hesaplamalar
        int totalItems = cartItems.stream().mapToInt(CartIndex.CartItemIndex::getQuantity).sum();
        double totalPrice = cartItems.stream().mapToDouble(CartIndex.CartItemIndex::getSubtotal).sum();

        CartIndex cartIndex = CartIndex.builder()
                .id(cart.getId().toString())
                .userId(cart.getUser().getId().toString())
                .userName(cart.getUser().getUsername())
                .items(cartItems)
                .totalItems(totalItems)
                .totalPrice(totalPrice)
                .build();

        cartSearchRepository.save(cartIndex);
        log.info("Sepet başarıyla indekslendi id: {}", cartIndex.getId());
    }

    private CartIndex.CartItemIndex convertToCartItemIndex(CartLine cartLine) {
        double subtotal = cartLine.getQuantity() * cartLine.getProduct().getPrice();

        return CartIndex.CartItemIndex.builder()
                .productId(cartLine.getProduct().getId().toString())
                .productName(cartLine.getProduct().getName())
                .productPrice(cartLine.getProduct().getPrice())
                .quantity(cartLine.getQuantity())
                .subtotal(subtotal)
                .categoryId(cartLine.getProduct().getCategory() != null
                        ? cartLine.getProduct().getCategory().getId().toString()
                        : null)
                .categoryName(
                        cartLine.getProduct().getCategory() != null ? cartLine.getProduct().getCategory().getName()
                                : null)
                .build();
    }

    public Page<CartIndex> getAll(Pageable pageable) {
        log.info("Tüm sepetler Elasticsearch'ten getiriliyor...");
        return cartSearchRepository.findAll(pageable);
    }

    public Optional<CartIndex> getById(Long id) {
        log.info("Sepet Elasticsearch'ten getiriliyor id: {}", id);
        return cartSearchRepository.findById(id.toString());
    }

    public Optional<CartIndex> getByUserId(Long userId) {
        log.info("Sepet Elasticsearch'ten getiriliyor kullanıcı id: {}", userId);
        return cartSearchRepository.findByUserId(userId.toString());
    }

    public Page<CartIndex> searchByUserName(String userName, Pageable pageable) {
        log.info("Sepetler Elasticsearch'ten kullanıcı adı ile aranıyor: '{}'", userName);
        return cartSearchRepository.searchByUserName(userName, pageable);
    }

    public Page<CartIndex> findCartsWithProduct(Long productId, Pageable pageable) {
        log.info("Ürün içeren sepetler Elasticsearch'ten bulunuyor: {}", productId);
        return cartSearchRepository.findByProductId(productId.toString(), pageable);
    }

    public Page<CartIndex> findCartsByPriceRange(double minPrice, double maxPrice, Pageable pageable) {
        log.info("Fiyat aralığındaki sepetler Elasticsearch'ten bulunuyor: {}-{}", minPrice, maxPrice);
        return cartSearchRepository.findByTotalPriceBetween(minPrice, maxPrice, pageable);
    }

    public void deleteFromIndex(Long cartId) {
        log.info("Sepet Elasticsearch'ten siliniyor: {}", cartId);
        cartSearchRepository.deleteById(cartId.toString());
        log.info("Sepet başarıyla Elasticsearch'ten silindi");
    }

    public void deleteByUserId(Long userId) {
        log.info("Kullanıcı sepeti Elasticsearch'ten siliniyor: {}", userId);
        Optional<CartIndex> cartIndex = cartSearchRepository.findByUserId(userId.toString());
        if (cartIndex.isPresent()) {
            cartSearchRepository.deleteById(cartIndex.get().getId());
            log.info("Kullanıcı sepeti başarıyla Elasticsearch'ten silindi");
        } else {
            log.warn("Kullanıcı sepeti Elasticsearch'te bulunamadı: {}", userId);
        }
    }
}
