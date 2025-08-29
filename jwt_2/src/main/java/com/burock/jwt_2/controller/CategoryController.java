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

import com.burock.jwt_2.dto.ResponseWrapper;
import com.burock.jwt_2.model.Category;
import com.burock.jwt_2.search.model.CategoryIndex;
import com.burock.jwt_2.service.CategoryService;
import com.burock.jwt_2.service.MessageService;

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
@RequestMapping("/categories")
@Tag(name = "Kategoriler", description = "Kategori yönetimi ve Elasticsearch arama işlemleri")
public class CategoryController {

        private final CategoryService service;
        private final MessageService messageService;

        // Herkes

        @Operation(summary = "Tüm Kategorileri Listele", description = "Elasticsearch'ten sayfalı olarak tüm kategorileri getirir")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Kategoriler başarıyla listelendi", content = @Content(schema = @Schema(implementation = Page.class))),
                        @ApiResponse(responseCode = "500", description = "Sunucu hatası")
        })
        @GetMapping
        public ResponseEntity<Page<CategoryIndex>> getAllCategories(
                        @Parameter(description = "Sayfa numarası (0'dan başlar)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Sayfa başına kayıt sayısı") @RequestParam(defaultValue = "20") int size) {
                return ResponseEntity.ok(service.getAll(PageRequest.of(page, size)));
        }

        @Operation(summary = "ID ile Kategori Getir", description = "Belirtilen ID'ye sahip kategoriyi getirir")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Kategori başarıyla getirildi", content = @Content(schema = @Schema(implementation = CategoryIndex.class))),
                        @ApiResponse(responseCode = "404", description = "Kategori bulunamadı")
        })
        @GetMapping("/{id}")
        public ResponseEntity<CategoryIndex> getCategoryById(
                        @Parameter(description = "Kategori ID'si", required = true) @PathVariable Long id) {
                return ResponseEntity.ok(service.getById(id));
        }

        @Operation(summary = "Kategori Ara", description = "İsim bazında fuzzy search yapar. Tam eşleşme gerekmez.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Arama başarılı"),
                        @ApiResponse(responseCode = "400", description = "Geçersiz arama parametresi")
        })
        @GetMapping("/search")
        public ResponseEntity<Page<CategoryIndex>> searchCategories(
                        @Parameter(description = "Arama terimi (kategori adı)") @RequestParam String name,
                        @Parameter(description = "Sayfa numarası") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Sayfa boyutu") @RequestParam(defaultValue = "20") int size) {
                return ResponseEntity.ok(service.searchCategories(name, PageRequest.of(page, size)));
        }

        @Operation(summary = "İsim ile Kategori Getir", description = "Belirtilen isme sahip kategoriyi getirir")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Kategori başarıyla getirildi", content = @Content(schema = @Schema(implementation = CategoryIndex.class))),
                        @ApiResponse(responseCode = "404", description = "Kategori bulunamadı")
        })
        @GetMapping("/name/{name}")
        public ResponseEntity<CategoryIndex> getCategoryByName(
                        @Parameter(description = "Kategori adı", required = true) @PathVariable String name) {
                return ResponseEntity.ok(service.findByName(name));
        }

        @Operation(summary = "Yeni Kategori Oluştur", description = "Sadece Admin kullanıcılar yeni kategori oluşturabilir", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Kategori başarıyla oluşturuldu", content = @Content(schema = @Schema(implementation = Category.class))),
                        @ApiResponse(responseCode = "400", description = "Geçersiz kategori bilgileri"),
                        @ApiResponse(responseCode = "403", description = "Admin yetkisi gerekli")
        })
        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping
        public ResponseEntity<ResponseWrapper<Category>> createCategory(
                        @Parameter(description = "Kategori bilgileri", required = true) @Valid @RequestBody Category c) {
                try {
                        Category createdCategory = service.create(c);
                        return ResponseEntity.ok(new ResponseWrapper<>(
                                        messageService.getMessage("category.created"),
                                        createdCategory));
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                                        messageService.getMessage("error"),
                                        null));
                }
        }

        @Operation(summary = "Kategori Güncelle", description = "Sadece Admin kullanıcılar kategori güncelleyebilir", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Kategori başarıyla güncellendi", content = @Content(schema = @Schema(implementation = Category.class))),
                        @ApiResponse(responseCode = "400", description = "Geçersiz kategori bilgileri"),
                        @ApiResponse(responseCode = "404", description = "Kategori bulunamadı"),
                        @ApiResponse(responseCode = "403", description = "Admin yetkisi gerekli")
        })
        @PreAuthorize("hasRole('ADMIN')")
        @PutMapping("/{id}")
        public ResponseEntity<ResponseWrapper<Category>> updateCategory(
                        @Parameter(description = "Kategori ID'si", required = true) @PathVariable Long id,
                        @Parameter(description = "Güncellenmiş kategori bilgileri", required = true) @Valid @RequestBody Category c) {
                try {
                        Category updatedCategory = service.update(id, c);
                        return ResponseEntity.ok(new ResponseWrapper<>(
                                        messageService.getMessage("category.updated"),
                                        updatedCategory));
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                                        messageService.getMessage("category.not.found"),
                                        null));
                }
        }

        @Operation(summary = "Kategori Sil", description = "Sadece Admin kullanıcılar kategori silebilir", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Kategori başarıyla silindi"),
                        @ApiResponse(responseCode = "404", description = "Kategori bulunamadı"),
                        @ApiResponse(responseCode = "403", description = "Admin yetkisi gerekli")
        })
        @PreAuthorize("hasRole('ADMIN')")
        @DeleteMapping("/{id}")
        public ResponseEntity<ResponseWrapper<Void>> deleteCategory(
                        @Parameter(description = "Silinecek kategori ID'si", required = true) @PathVariable Long id) {
                try {
                        service.delete(id);
                        return ResponseEntity.ok(new ResponseWrapper<>(
                                        messageService.getMessage("category.deleted"),
                                        null));
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                                        messageService.getMessage("category.not.found"),
                                        null));
                }
        }
}
