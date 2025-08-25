package com.burock.jwt_2.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.burock.jwt_2.model.Category;
import com.burock.jwt_2.repository.CategoryRepository;
import com.burock.jwt_2.search.model.CategoryIndex;
import com.burock.jwt_2.search.service.CategorySearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoryService {

    private final CategoryRepository repo;
    private final CategorySearchService categorySearchService;

    // GET işlemleri ElasticSearch ile

    public Page<CategoryIndex> getAll(Pageable pageable) {
        log.info("Tüm kategoriler Elasticsearch'ten getiriliyor...");
        return categorySearchService.getAll(pageable);
    }

    public CategoryIndex getById(Long id) {
        return categorySearchService.getById(id);
    }

    public Page<CategoryIndex> searchCategories(String name, Pageable pageable) {
        log.info("Kategoriler Elasticsearch ile aranıyor isim: '{}'", name);
        return categorySearchService.searchByName(name, pageable);
    }

    public CategoryIndex findByName(String name) {
        log.info("Kategori Elasticsearch ile tam isimle bulunuyor: '{}'", name);
        return categorySearchService.findByName(name);
    }

    // Sadece Admin, CUD işlemleri

    @PreAuthorize("hasRole('ADMIN')")
    public Category create(Category c) {
        log.info("Yeni kategori oluşturuluyor: {}", c.getName());

        Category saved = repo.save(c);
        log.info("Kategori veritabanına kaydedildi id: {}", saved.getId());

        try {
            categorySearchService.indexCategory(saved);
            log.info("Kategori Elasticsearch'e başarıyla indekslendi");
        } catch (Exception e) {
            log.error("Kategori Elasticsearch'e indekslenemedi: {}", e.getMessage());
        }
        return saved;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Category update(Long id, Category c) {
        log.info("Kategori güncelleniyor id: {}", id);
        Category ec = repo.findById(id).orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + id));
        ec.setName(c.getName());

        Category updated = repo.save(ec);
        log.info("Kategori veritabanında başarıyla güncellendi");

        try {
            categorySearchService.indexCategory(updated);
            log.info("Kategori Elasticsearch'te başarıyla güncellendi");
        } catch (Exception e) {
            log.error("Kategori Elasticsearch'te güncellenemedi: {}", e.getMessage());
        }
        return updated;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        log.info("Kategori siliniyor id: {}", id);

        if (!repo.existsById(id)) {
            throw new RuntimeException("Kategori bulunamadı: " + id);
        }
        repo.deleteById(id);
        log.info("Kategori database'den silindi");

        try {
            categorySearchService.deleteFromIndex(id);
            log.info("Kategori Elasticsearch'ten başarıyla silindi");
        } catch (Exception e) {
            log.error("Kategori Elasticsearch'ten silinemedi: {}", e.getMessage());
        }
    }

    public Category getByIdFromDatabase(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Kategori bulunamadı" + id));
    }
}
