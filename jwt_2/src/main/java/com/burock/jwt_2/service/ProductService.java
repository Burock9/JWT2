package com.burock.jwt_2.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.burock.jwt_2.model.Product;
import com.burock.jwt_2.repository.ProductRepository;
import com.burock.jwt_2.search.service.ProductSearchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository repo;
    private final ProductSearchService productSearchService;

    public List<Product> getAll() {
        return repo.findAll();
    }

    public Product getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Ürün Bulunamadı."));
    }

    // Sadece Admin

    @PreAuthorize("hasRole('ADMIN')")
    public Product create(Product p) {

        Product saved = repo.save(p);
        productSearchService.indexProduct(saved);
        return saved;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Product update(Long id, Product p) {
        Product ep = getById(id);
        ep.setName(p.getName());
        ep.setPrice(p.getPrice());
        ep.setCategory(p.getCategory());
        ep.setStock(p.getStock());
        Product saved = repo.save(p);
        productSearchService.indexProduct(saved);
        return saved;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        repo.deleteById(id);
        productSearchService.deleteFromIndex(id);
    }
}
