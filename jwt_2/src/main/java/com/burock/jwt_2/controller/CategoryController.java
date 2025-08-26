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

import com.burock.jwt_2.dto.ApiResponse;
import com.burock.jwt_2.model.Category;
import com.burock.jwt_2.search.model.CategoryIndex;
import com.burock.jwt_2.service.CategoryService;
import com.burock.jwt_2.service.MessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService service;
    private final MessageService messageService;

    // Herkes

    @GetMapping
    public ResponseEntity<Page<CategoryIndex>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getAll(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryIndex> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CategoryIndex>> searchCategories(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.searchCategories(name, PageRequest.of(page, size)));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<CategoryIndex> getCategoryByName(@PathVariable String name) {
        return ResponseEntity.ok(service.findByName(name));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(@Valid @RequestBody Category c) {
        try {
            Category createdCategory = service.create(c);
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("category.created"),
                    createdCategory));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable Long id, @Valid @RequestBody Category c) {
        try {
            Category updatedCategory = service.update(id, c);
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("category.updated"),
                    updatedCategory));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("category.not.found"),
                    null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("category.deleted"),
                    null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("category.not.found"),
                    null));
        }
    }
}
