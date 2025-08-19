package com.burock.jwt_2.service;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.burock.jwt_2.model.User;
import com.burock.jwt_2.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public User save(User u) {
        return repo.save(u);
    }

    public boolean exists(String username) {
        return repo.existsByUsername(username);
    }

    @PostAuthorize("returnObject.username == authentication.name or hasRole('ADMIN')")
    public User getByUsernameSecured(String username) {
        return repo.findByUsername(username).orElseThrow(() -> new RuntimeException("Kullanıcı Bulunamadı."));
    }

    public String encode(String raw) {
        return encoder.encode(raw);
    }
}
