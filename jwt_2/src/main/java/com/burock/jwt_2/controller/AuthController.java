package com.burock.jwt_2.controller;

import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.burock.jwt_2.dto.ApiResponse;
import com.burock.jwt_2.dto.LoginRequest;
import com.burock.jwt_2.dto.TokenResponse;
import com.burock.jwt_2.model.User;
import com.burock.jwt_2.service.AuthService;
import com.burock.jwt_2.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MessageService messageService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody LoginRequest req) {
        try {
            String result = authService.register(req);
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("auth.register.success"),
                    result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("auth.register.failed"),
                    null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest req) {
        try {
            TokenResponse tokenResponse = authService.login(req);
            return ResponseEntity.ok(new ApiResponse<>(
                    messageService.getMessage("auth.login.success"),
                    tokenResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    messageService.getMessage("auth.login.failed"),
                    null));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<User> me(Principal principal) {
        return ResponseEntity.ok(authService.me(principal.getName()));
    }
}
