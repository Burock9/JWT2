package com.burock.jwt_2.search.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.burock.jwt_2.model.Product;
import com.burock.jwt_2.search.model.ProductIndex;
import com.burock.jwt_2.search.repository.ProductSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductSearchRepository productSearchRepository;

    public void indexProduct(Product p) {
        ProductIndex doc = ProductIndex.builder().id(p.getId().toString()).name(p.getName()).price(p.getPrice())
                .stock(p.getStock())
                .categoryId(p.getCategory() != null ? String.valueOf(p.getCategory().getId()) : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null).build();

        productSearchRepository.save(doc);
    }

    public Page<ProductIndex> getAll(Pageable pageable) {
        return productSearchRepository.findAll(pageable);
    }

    public ProductIndex getById(Long id) {
        return productSearchRepository.findById(id.toString()).orElseThrow(() -> new RuntimeException("Ürün Bulunamadı:"+ id));
    }

    public Page<ProductIndex> search(String q, Pageable pageable) {
        return productSearchRepository.searchByText(q, pageable);
    }

    public Page<ProductIndex> byCategory(Long categoryId, Pageable pageable) {
        return productSearchRepository.findByCategoryId(String.valueOf(categoryId), pageable);
    }

    public void deleteFromIndex(Long productId) {
        productSearchRepository.deleteById(productId.toString());
    }
}
