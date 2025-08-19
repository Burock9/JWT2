package com.burock.jwt_2.service;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final Key key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, Collection<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder().setSubject(username).setIssuedAt(now).setExpiration(exp).addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        Claims claims = getClaims(token);
        Object r = claims.get("roles");
        if (r instanceof Collection<?>) {
            return ((Collection<?>) r).stream().map(Object::toString).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
