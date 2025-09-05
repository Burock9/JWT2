package com.burock.jwt_2.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.burock.jwt_2.dto.ResponseWrapper;
import com.burock.jwt_2.model.User;
import com.burock.jwt_2.service.MessageService;
import com.burock.jwt_2.service.UserService;

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
@RequestMapping("/admin/users")
@Tag(name = "Kullanıcı Yönetimi", description = "Admin kullanıcı işlemleri")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final MessageService messageService;

    @Operation(summary = "Tüm Kullanıcıları Listele", description = "Sadece ADMIN kullanıcılar tüm kullanıcıları görebilir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kullanıcılar başarıyla listelendi", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "403", description = "Admin yetkisi gerekli")
    })
    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(
            @Parameter(description = "Sayfa numarası (0'dan başlar)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sayfa başına kayıt sayısı") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.getAllUsers(PageRequest.of(page, size)));
    }

    @Operation(summary = "Kullanıcı Detaylarını Getir", description = "ID ile kullanıcı bilgilerini getirir")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Kullanıcı Güncelle", description = "Kullanıcı bilgilerini günceller")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<User>> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("user.updated"),
                    updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("user.not.found"),
                    null));
        }
    }

    @Operation(summary = "Kullanıcı Sil", description = "Kullanıcıyı sistemden siler")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("user.deleted"),
                    null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("user.not.found"),
                    null));
        }
    }

    @Operation(summary = "Kullanıcı Ara", description = "Kullanıcı adı veya e-posta ile arama yapar")
    @GetMapping("/search")
    public ResponseEntity<Page<User>> searchUsers(
            @Parameter(description = "Arama terimi") @RequestParam String q,
            @Parameter(description = "Sayfa numarası") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sayfa boyutu") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.searchUsers(q, PageRequest.of(page, size)));
    }
}
