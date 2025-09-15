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
import com.burock.jwt_2.search.service.CategorySearchService;
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
    private final CategorySearchService categorySearchService;

    public Page<ProductIndex> getAll(Pageable pageable) {
        log.info("Tüm ürünler Elasticsearch ile getiriliyor...");
        return productSearchService.getAll(pageable);
    }

    public Page<Product> getAllForAdmin(Pageable pageable) {
        log.info("Admin için tüm ürünler database'den getiriliyor...");
        return repo.findAll(pageable);
    }

    public Product getProductById(Long id) {
        log.info("Ürün database'den getiriliyor: {}", id);
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Ürün bulunamadı: " + id));
    }

    // Dashboard için istatistik metodları
    public long getTotalProductCount() {
        return repo.count();
    }

    public long getOutOfStockCount() {
        return repo.countByStockLessThanEqual(0);
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
        log.info("Elasticsearch ile {} ile {} arası fiyatlardaki ürünler bulunuyor...", minPrice, maxPrice);
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
            // Kategori index'ini güncelleyelim (ürün sayısı için)
            if (saved.getCategory() != null) {
                categorySearchService.indexCategory(saved.getCategory());
            }
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

        // Eski kategoriyi kaydet (kategori değişirse güncellemek için)
        Category oldCategory = ep.getCategory();

        if (p.getCategory() != null && p.getCategory().getId() != null) {
            Category fullCategory = categoryRepo.findById(p.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + p.getCategory().getId()));
            p.setCategory(fullCategory);
        }
        ep.setName(p.getName());
        ep.setPrice(p.getPrice());
        ep.setCategory(p.getCategory());
        ep.setStock(p.getStock());
        ep.setImageUrl(p.getImageUrl());
        ep.setDescription(p.getDescription());

        Product saved = repo.save(ep);
        try {
            productSearchService.indexProduct(saved);

            // Kategori index'lerini güncelleyelim (ürün sayısı için)
            // Yeni kategoriyi güncelle
            if (saved.getCategory() != null) {
                categorySearchService.indexCategory(saved.getCategory());
            }

            // Eğer kategori değiştiyse, eski kategoriyi de güncelle
            if (oldCategory != null &&
                    (saved.getCategory() == null || !oldCategory.getId().equals(saved.getCategory().getId()))) {
                categorySearchService.indexCategory(oldCategory);
            }

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

        // Kategori bilgisini silinmeden önce al
        Product productToDelete = repo.findById(id).orElse(null);
        Category categoryToUpdate = productToDelete != null ? productToDelete.getCategory() : null;

        repo.deleteById(id);

        try {
            productSearchService.deleteFromIndex(id);
            // Kategori index'ini güncelleyelim (ürün sayısı için)
            if (categoryToUpdate != null) {
                categorySearchService.indexCategory(categoryToUpdate);
            }
            log.info("Ürün Elastiksearch'ten başarıyla silindi");
        } catch (Exception e) {
            log.error("Elastiksearch'ten ürün silme başarısız oldu: {}", e.getMessage());
        }
    }
}
