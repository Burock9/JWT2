package com.burock.jwt_2.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.burock.jwt_2.dto.ResponseWrapper;
import com.burock.jwt_2.model.Product;
import com.burock.jwt_2.search.model.ProductIndex;
import com.burock.jwt_2.service.MessageService;
import com.burock.jwt_2.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
@Tag(name = "Ürünler", description = "Ürün işlemleri")
public class ProductController {

    private final ProductService service;
    private final MessageService messageService;
    // Herkes

    @Operation(summary = "Tüm Ürünleri Listele", description = "Elasticsearch'ten sayfalı olarak tüm ürünleri getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ürünler başarıyla listelendi", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    @GetMapping
    public ResponseEntity<Page<ProductIndex>> getAllProducts(
            @Parameter(description = "Sayfa numarası (0'dan başlar)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sayfa başına kayıt sayısı") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getAll(PageRequest.of(page, size)));
    }

    @Operation(summary = "Ürün Ara", description = "İsim ve kategori adında fuzzy search yapar. Tam eşleşme gerekmez.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arama başarılı"),
            @ApiResponse(responseCode = "400", description = "Geçersiz arama parametresi")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ProductIndex>> searchProducts(
            @Parameter(description = "Arama terimi (ürün adı veya kategori)") @RequestParam String q,
            @Parameter(description = "Sayfa numarası") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sayfa boyutu") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.searchProducts(q, PageRequest.of(page, size)));
    }

    // Admin

    @Operation(summary = "Yeni Ürün Oluştur", description = "Sadece ADMIN kullanıcılar yeni ürün oluşturabilir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ürün başarıyla oluşturuldu", content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz ürün bilgileri"),
            @ApiResponse(responseCode = "403", description = "Admin yetkisi gerekli")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ResponseWrapper<Product>> create(
            @Parameter(description = "Ürün bilgileri", required = true) @Valid @RequestBody Product p) {
        try {
            Product createdProduct = service.create(p);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("product.created"),
                    createdProduct));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @Operation(summary = "Ürün Güncelle", description = "Sadece Admin kullanıcılar ürün güncelleyebilir", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Product>> update(@PathVariable Long id, @Valid @RequestBody Product p) {
        try {
            Product updatedProduct = service.update(id, p);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("product.updated"),
                    updatedProduct));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("product.not.found"),
                    null));
        }
    }

    @Operation(summary = "Ürün Sil", description = "Sadece Admin kullanıcılar ürün silebilir", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("product.deleted"),
                    null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("product.not.found"),
                    null));
        }
    }

    @Operation(summary = "Kategoriye Göre Ara", description = "Kategoriye göre ürün arar.")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductIndex>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getProductsByCategory((categoryId), PageRequest.of(page, size)));
    }

    @Operation(summary = "ID ile Ara", description = "Ürün ID numarasına göre arar.")
    @GetMapping("/{id}")
    public ResponseEntity<ProductIndex> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Fiyata Göre Ara", description = "Belirli fiyat aralıklarına göre arar.")
    @GetMapping("/price-range")
    public ResponseEntity<Page<ProductIndex>> getProductsByPriceRange(
            @RequestParam double minPrice,
            @RequestParam double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.findByPriceRange(minPrice, maxPrice, PageRequest.of(page, size)));
    }

    @Operation(summary = "Stok Olanları Ara", description = "Stokta olan ürünleri arar.")
    @GetMapping("/in-stock")
    public ResponseEntity<Page<ProductIndex>> getInStockProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.findInStock(PageRequest.of(page, size)));
    }
}
