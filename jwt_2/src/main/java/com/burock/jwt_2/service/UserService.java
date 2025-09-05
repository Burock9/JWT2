package com.burock.jwt_2.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public boolean existsByEmail(String email) {
        return repo.existsByEmail(email);
    }

    @PostAuthorize("returnObject.username == authentication.name or hasRole('ADMIN')")
    public User getByUsernameSecured(String username) {
        return repo.findByUsername(username).orElseThrow(() -> new RuntimeException("Kullanıcı Bulunamadı."));
    }

    public String encode(String raw) {
        return encoder.encode(raw);
    }

    // Admin işlemleri için metodlar
    public Page<User> getAllUsers(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public User getUserById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
    }

    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(encode(user.getPassword()));
        }
        if (user.getRoles() != null) {
            existingUser.setRoles(user.getRoles());
        }
        return repo.save(existingUser);
    }

    public void deleteUser(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Kullanıcı bulunamadı");
        }
        repo.deleteById(id);
    }

    public Page<User> searchUsers(String query, Pageable pageable) {
        return repo.findByUsernameContainingOrEmailContaining(query, query, pageable);
    }

    // Dashboard için istatistik metodları
    public long getTotalUserCount() {
        return repo.count();
    }
}
