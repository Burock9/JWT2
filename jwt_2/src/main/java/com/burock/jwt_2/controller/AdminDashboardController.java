package com.burock.jwt_2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.burock.jwt_2.dto.ResponseWrapper;
import com.burock.jwt_2.service.ProductService;
import com.burock.jwt_2.service.UserService;
import com.burock.jwt_2.repository.CategoryRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/dashboard")
@Tag(name = "Admin Dashboard", description = "Admin dashboard istatistikleri")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardController {

    private final ProductService productService;
    private final UserService userService;
    private final CategoryRepository categoryRepository;

    @Operation(summary = "Dashboard İstatistikleri", description = "Admin paneli için özet istatistikler")
    @GetMapping("/stats")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Toplam ürün sayısı
            long totalProducts = productService.getTotalProductCount();
            stats.put("totalProducts", totalProducts);
            
            // Toplam kullanıcı sayısı
            long totalUsers = userService.getTotalUserCount();
            stats.put("totalUsers", totalUsers);
            
            // Toplam kategori sayısı
            long totalCategories = categoryRepository.count();
            stats.put("totalCategories", totalCategories);
            
            // Stokta olmayan ürün sayısı
            long outOfStockProducts = productService.getOutOfStockCount();
            stats.put("outOfStockProducts", outOfStockProducts);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    "Dashboard istatistikleri başarıyla getirildi",
                    stats));
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ResponseWrapper<>(
                    "İstatistikler alınırken hata oluştu",
                    null));
        }
    }
}
