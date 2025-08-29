package com.burock.jwt_2.controller;

import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.burock.jwt_2.dto.ResponseWrapper;
import com.burock.jwt_2.dto.LoginRequest;
import com.burock.jwt_2.dto.TokenResponse;
import com.burock.jwt_2.model.User;
import com.burock.jwt_2.service.AuthService;
import com.burock.jwt_2.service.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Kimlik Doğrulama", description = "Kullanıcı kayıt ve giriş işlemleri")
public class AuthController {

    private final AuthService authService;
    private final MessageService messageService;

    @Operation(summary = "Kullanıcı Kaydı", description = "Yeni kullanıcı hesabı oluşturur. Varsayılan olarak USER rolü atanır.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kayıt başarılı"),
            @ApiResponse(responseCode = "400", description = "Geçersiz veri")
    })
    @PostMapping("/register")
    public ResponseEntity<ResponseWrapper<String>> register(@Valid @RequestBody LoginRequest req) {
        try {
            String result = authService.register(req);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("auth.register.success"),
                    result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("auth.register.failed"),
                    null));
        }
    }

    @Operation(summary = "Kullanıcı Girişi", description = "Username ve şifre ile giriş yaparak JWT token alabilirsiniz.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Giriş başarılı", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "Geçersiz kimlik bilgileri")
    })
    @PostMapping("/login")
    public ResponseEntity<ResponseWrapper<TokenResponse>> login(@Valid @RequestBody LoginRequest req) {
        try {
            TokenResponse tokenResponse = authService.login(req);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("auth.login.success"),
                    tokenResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("auth.login.failed"),
                    null));
        }
    }

    @Operation(summary = "Kullanıcı Profili", description = "Giriş yapmış kullanıcının profil bilgilerini getirir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil bilgisi başarıyla getirildi", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @GetMapping("/me")
    public ResponseEntity<User> me(Principal principal) {
        return ResponseEntity.ok(authService.me(principal.getName()));
    }
}
