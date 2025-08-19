package com.burock.jwt_2.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.burock.jwt_2.dto.LoginRequest;
import com.burock.jwt_2.dto.TokenResponse;
import com.burock.jwt_2.model.Role;
import com.burock.jwt_2.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserService userService;

    public String register(LoginRequest req) {
        if (userService.exists(req.getUsername())) {
            throw new RuntimeException("Kullanıcı adı zaten var.");
        }

        User u = User.builder().username(req.getUsername()).password(userService.encode(req.getPassword()))
                .roles(Set.of(Role.ROLE_USER)).build();
        userService.save(u);
        return "Kayıt Başarılı";
    }

    public TokenResponse login(LoginRequest req) {
        Authentication auth = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        var roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        String token = jwtService.generateToken(req.getUsername(), roles);

        return TokenResponse.builder().token(token).build();
    }

    public User me(String username) {
        return userService.getByUsernameSecured(username);
    }

}
