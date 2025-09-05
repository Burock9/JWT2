package com.burock.jwt_2.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.burock.jwt_2.dto.ResponseWrapper;
import com.burock.jwt_2.model.Product;
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
@RequestMapping("/admin/products")
@Tag(name = "Admin Ürün Yönetimi", description = "Admin ürün işlemleri")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminProductController {

    private final ProductService productService;
    private final MessageService messageService;

    @Operation(summary = "Admin Ürün Listesi", description = "Sadece ADMIN kullanıcılar için tüm ürünleri listeler")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ürünler başarıyla listelendi", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "403", description = "Admin yetkisi gerekli")
    })
    @GetMapping
    public ResponseEntity<ResponseWrapper<Page<Product>>> getAllProductsForAdmin(
            @Parameter(description = "Sayfa numarası (0'dan başlar)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sayfa başına kayıt sayısı") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(new ResponseWrapper<>(
                "Ürünler başarıyla listelendi",
                productService.getAllForAdmin(PageRequest.of(page, size))));
    }

    @Operation(summary = "Yeni Ürün Oluştur", description = "Yeni ürün oluşturur")
    @PostMapping
    public ResponseEntity<ResponseWrapper<Product>> createProduct(
            @Parameter(description = "Ürün bilgileri", required = true) @Valid @RequestBody Product product) {
        try {
            Product createdProduct = productService.create(product);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("product.created"),
                    createdProduct));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @Operation(summary = "Ürün Güncelle", description = "Ürün bilgilerini günceller")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Product>> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        try {
            Product updatedProduct = productService.update(id, product);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("product.updated"),
                    updatedProduct));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("product.not.found"),
                    null));
        }
    }

    @Operation(summary = "Ürün Sil", description = "Ürünü sistemden siler")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> deleteProduct(@PathVariable Long id) {
        try {
            productService.delete(id);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("product.deleted"),
                    null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("product.not.found"),
                    null));
        }
    }

    @Operation(summary = "Ürün Detayını Getir", description = "ID ile ürün bilgilerini getirir")
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            // Database'den Product döndürüyoruz, Elasticsearch'ten değil
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
