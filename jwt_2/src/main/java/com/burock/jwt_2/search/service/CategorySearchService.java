package com.burock.jwt_2.search.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.burock.jwt_2.model.Category;
import com.burock.jwt_2.search.model.CategoryIndex;
import com.burock.jwt_2.search.repository.CategorySearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategorySearchService {

    private final CategorySearchRepository categorySearchRepository;

    public void indexCategory(Category category) {
        log.info("Kategori Elasticsearch'e indeksleniyor: {}", category.getName());

        CategoryIndex categoryIndex = CategoryIndex.builder().id(category.getId().toString()).name(category.getName())
                .description(null).productCount(0).build();

        categorySearchRepository.save(categoryIndex);
        log.info("Kategori başarıyla indekslendi", categoryIndex.getId());
    }

    public Page<CategoryIndex> getAll(Pageable pageable) {
        log.info("Tüm kategoriler Elasticsearch'ten getiriliyor...");
        return categorySearchRepository.findAll(pageable);
    }

    public CategoryIndex getById(Long id) {
        log.info("Id: {} Kategori Elasticsearch'ten getiriliyor...", id);
        return categorySearchRepository.findById(id.toString())
                .orElseThrow(() -> new RuntimeException("Kategori bulunamadı:" + id));
    }

    public Page<CategoryIndex> searchByName(String name, Pageable pageable) {
        log.info("Kategoriler Elasticsearch ile isimle aranıyor: '{}' ", name);
        return categorySearchRepository.searchByName(name, pageable);
    }

    public CategoryIndex findByName(String name) {
        log.info("Kategori Elasticsearch ile tam isimle bulunuyor: '{}' ", name);
        return categorySearchRepository.findByName(name);
    }

    public void deleteFromIndex(Long categoryId) {
        log.info("Kategori Elasticsearch'ten siliniyor: {}", categoryId);
        categorySearchRepository.deleteById(categoryId.toString());
        log.info("Kategori Elasticsearch'ten silindi.");
    }

    public void updateProductCount(Long categoryId, int newCount) {
        log.info("Kategori ürün sayısı güncelleniyor: {} yeni sayı: {}", categoryId, newCount);

        CategoryIndex categoryIndex = categorySearchRepository.findById(categoryId.toString()).orElse(null);

        if (categoryIndex != null) {
            categoryIndex.setProductCount(newCount);
            categorySearchRepository.save(categoryIndex);
            log.info("Ürün sayısı başarıyla güncellendi.");
        } else {
            log.warn("Kategori Elasticsearch'te bulunamadı id: {}", categoryId);
        }
    }
}
