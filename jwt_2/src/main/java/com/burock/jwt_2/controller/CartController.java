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
import com.burock.jwt_2.dto.ResponseWrapper;
import com.burock.jwt_2.dto.CartResponse;
import com.burock.jwt_2.model.User;
import com.burock.jwt_2.search.model.CartIndex;
import com.burock.jwt_2.service.CartService;
import com.burock.jwt_2.service.MessageService;
import com.burock.jwt_2.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Sepet", description = "Sepet yönetimi ve Elasticsearch arama işlemleri")
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final MessageService messageService;

    @Operation(summary = "Sepete Ürün Ekle", description = "Kullanıcının sepetine belirtilen miktarda ürün ekler", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ürün sepete başarıyla eklendi"),
            @ApiResponse(responseCode = "400", description = "Geçersiz veri veya yetersiz stok"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @PostMapping("/add")
    public ResponseEntity<ResponseWrapper<String>> addToCart(
            @Parameter(description = "Sepete eklenecek ürün bilgileri", required = true) @RequestBody AddToCartRequest request,
            Principal principal) {
        try {
            User user = userService.getByUsernameSecured(principal.getName());
            cartService.addToCart(user, request);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("cart.item.added"),
                    null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @Operation(summary = "Sepeti Görüntüle", description = "Kullanıcının mevcut sepet içeriğini getirir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sepet başarıyla getirildi", content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @GetMapping
    public ResponseEntity<CartResponse> getCart(Principal principal) {
        User user = userService.getByUsernameSecured(principal.getName());
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @Operation(summary = "Elasticsearch'ten Sepet Getir", description = "Elasticsearch'ten kullanıcının sepet bilgilerini getirir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sepet Elasticsearch'ten başarıyla getirildi"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @GetMapping("/elasticsearch")
    public ResponseEntity<Optional<CartIndex>> getCartFromElasticsearch(Principal principal) {
        User user = userService.getByUsernameSecured(principal.getName());
        return ResponseEntity.ok(cartService.getCartByUserId(user.getId()));
    }

    @Operation(summary = "Sepetten Ürün Çıkar", description = "Kullanıcının sepetinden belirtilen ürünü tamamen çıkarır", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ürün sepetten başarıyla çıkarıldı"),
            @ApiResponse(responseCode = "400", description = "Ürün bulunamadı"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ResponseWrapper<String>> removeFromCart(
            @Parameter(description = "Çıkarılacak ürün ID'si", required = true) @PathVariable Long productId,
            Principal principal) {
        try {
            User user = userService.getByUsernameSecured(principal.getName());
            cartService.removeFromCart(user, productId);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("cart.item.removed"),
                    null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @Operation(summary = "Sepet Analizi", description = "Kullanıcının sepet analiz bilgilerini Elasticsearch'ten getirir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sepet analizi başarıyla getirildi", content = @Content(schema = @Schema(implementation = CartIndex.class))),
            @ApiResponse(responseCode = "404", description = "Sepet bulunamadı"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
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

    @Operation(summary = "Tüm Sepetleri Listele (Admin)", description = "Sadece Admin kullanıcılar tüm sepetleri listeleyebilir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sepetler başarıyla listelendi"),
            @ApiResponse(responseCode = "403", description = "Admin yetkisi gerekli"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all/carts")
    public ResponseEntity<Page<CartIndex>> getAllCarts(
            @Parameter(description = "Sayfa numarası") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sayfa boyutu") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(cartService.getAllCarts(PageRequest.of(page, size)));
    }

    @Operation(summary = "Kullanıcı Adına Göre Sepet Ara (Admin)", description = "Admin kullanıcılar kullanıcı adına göre sepet arayabilir", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/search")
    public ResponseEntity<Page<CartIndex>> searchCartsByUserName(
            @Parameter(description = "Aranacak kullanıcı adı") @RequestParam String userName,
            @Parameter(description = "Sayfa numarası") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sayfa boyutu") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(cartService.searchCartsByUserName(userName, PageRequest.of(page, size)));
    }

    @Operation(summary = "Ürün İçeren Sepetleri Bul (Admin)", description = "Belirtilen ürünü içeren sepetleri listeler", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/product/{productId}")
    public ResponseEntity<Page<CartIndex>> findCartsWithProduct(
            @Parameter(description = "Ürün ID'si") @PathVariable Long productId,
            @Parameter(description = "Sayfa numarası") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sayfa boyutu") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(cartService.findCartsWithProduct(productId, PageRequest.of(page, size)));
    }

    @Operation(summary = "Fiyat Aralığına Göre Sepet Bul (Admin)", description = "Belirtilen fiyat aralığındaki sepetleri listeler", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/price-range")
    public ResponseEntity<Page<CartIndex>> findCartsByPriceRange(
            @Parameter(description = "Minimum fiyat") @RequestParam double minPrice,
            @Parameter(description = "Maksimum fiyat") @RequestParam double maxPrice,
            @Parameter(description = "Sayfa numarası") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sayfa boyutu") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(cartService.findCartsByPriceRange(minPrice, maxPrice, PageRequest.of(page, size)));
    }
}
