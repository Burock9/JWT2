package com.burock.jwt_2.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.burock.jwt_2.model.Category;
import com.burock.jwt_2.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repo;

    public List<Category> getAll() {
        return repo.findAll();
    }

    public Category getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Kategori Bulunamadı."));
    }

    // Sadece Admin

    @PreAuthorize("hasRole('ADMIN')")
    public Category create(Category c) {
        return repo.save(c);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Category update(Long id, Category c) {
        Category ec = getById(id);
        ec.setName(c.getName());
        return repo.save(ec);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
