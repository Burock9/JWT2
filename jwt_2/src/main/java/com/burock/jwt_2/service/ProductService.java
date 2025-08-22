package com.burock.jwt_2.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.burock.jwt_2.model.Category;
import com.burock.jwt_2.model.Product;
import com.burock.jwt_2.repository.CategoryRepository;
import com.burock.jwt_2.repository.ProductRepository;
import com.burock.jwt_2.search.model.ProductIndex;
import com.burock.jwt_2.search.service.ProductSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductService {

    private final ProductRepository repo;
    private final ProductSearchService productSearchService;
    private final CategoryRepository categoryRepo;

    public Page<ProductIndex> getAll(Pageable pageable) {
        log.info("Tüm ürünler Elasticsearch ile getiriliyor...");
        return productSearchService.getAll(pageable);
    }

    public ProductIndex getById(Long id) {
        log.info("{} Id'li ürün Elasticsearch ile getiriliyor...", id);
        return productSearchService.getById(id);
    }

    public Page<ProductIndex> searchProducts(String query, Pageable pageable) {
        log.info("Ürünler sırayla aranıyor: '{}'", query);
        return productSearchService.search(query, pageable);
    }

    public Page<ProductIndex> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.info("Ürünler kategoriye göre getiriliyor: {}", categoryId);
        return productSearchService.byCategory(categoryId, pageable);
    }

    public Page<ProductIndex> findByPriceRange(double minPrice, double maxPrice, Pageable pageable) {
        log.info("Elasticsearch ile {} ile {} arası fiyatlardaki ürünler bulunuyor...", minPrice,maxPrice);
        return productSearchService.findByPriceRange(minPrice, maxPrice, pageable);
    }

    public Page<ProductIndex> findInStock(Pageable pageable) {
        log.info("Elastiksearch ile stoktaki ürünler bulunuyor...");
        return productSearchService.findInStock(pageable);
    }

    // Sadece Admin

    @PreAuthorize("hasRole('ADMIN')")
    public Product create(Product p) {
        log.info("Ürün oluşturuluyor: {}", p.getName());

        if (p.getCategory() != null && p.getCategory().getId() != null) {
            Category fullCategory = categoryRepo.findById(p.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + p.getCategory().getId()));
            p.setCategory(fullCategory);
        }
        Product saved = repo.save(p);
        try {
            productSearchService.indexProduct(saved);
            log.info("Ürün Elasticsearch'e başarılı bir şekilde indekslendi.");
        } catch (Exception e) {
            log.error("Ürün indekslenemedi: {}", e.getMessage());
        }
        return saved;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Product update(Long id, Product p) {
        log.info("Ürün güncelleniyor: {}", id);
        Product ep = repo.findById(id).orElseThrow(() -> new RuntimeException("Ürün bulunamadı: " + id));

        if (p.getCategory() != null && p.getCategory().getId() != null) {
            Category fullCategory = categoryRepo.findById(p.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + p.getCategory().getId()));
            p.setCategory(fullCategory);
        }
        ep.setName(p.getName());
        ep.setPrice(p.getPrice());
        ep.setCategory(p.getCategory());
        ep.setStock(p.getStock());

        Product saved = repo.save(ep);
        try {
            productSearchService.indexProduct(saved);
            log.info("Ürün Elasticsearch'te başarılı bir şekilde güncellendi.");
        } catch (Exception e) {
            log.error("Ürün güncellenemedi: {}", e.getMessage());
        }
        return saved;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        log.info("Ürün Siliniyor... : {}", id);

        if (!repo.existsById(id)) {
            throw new RuntimeException("Ürün bulunamadı: " + id);
        }
        repo.deleteById(id);

        try {
            productSearchService.deleteFromIndex(id);
            log.info("Ürün Elastiksearch'ten başarıyla silindi");
        } catch (Exception e) {
            log.error("Elastiksearch'ten ürün silme başarısız oldu: {}", e.getMessage());
        }
    }
}
