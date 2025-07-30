package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.security.impl.JwtAuthentication;
import io.jsonwebtoken.Claims;
import org.springframework.lang.Nullable;

import javax.crypto.SecretKey;

@SuppressWarnings({"unused"})
public interface JwtService {
    String generateToken(String subject, @Nullable User user);
    boolean validateToken(String token);
    SecretKey getSecretKey();
    Long getExpiration();
    Claims getClaims(String token);
    JwtAuthentication authenticate(Claims claims);
}
