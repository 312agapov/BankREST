package com.example.bankcards.security.impl;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

/**
 * Сервис для работы с JWT токенами (генерация, валидация, парсинг).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private static final String USER_ID_CLAIM_KEY = "userId";
    private static final String ROLENAME_CLAIM_KEY = "roleName";

    @Value("${jwt.access.secret}")
    protected String accessSecret;

    @Value("${jwt.access.exp_time}")
    protected String accessExpirationTime;

    /**
     * Генерирует JWT токен для пользователя.
     * @param subject имя пользователя (subject токена)
     * @param user объект пользователя
     * @return сгенерированный JWT токен
     * @throws NullPointerException если:
     *         - user = null
     *         - user.id = null
     *         - user.role = null
     */
    @Override
    public String generateToken(String subject, @Nullable User user) {
        if (user == null) {
            throw new NullPointerException("User object is null");
        }

        Date date = new Date();
        UUID userId = Optional.ofNullable(user.getId())
                .orElseThrow(() -> new NullPointerException("User ID is null"));
        String roleName = Optional.ofNullable(user.getRole())
                .map(Role::getName)
                .orElseThrow(() -> new NullPointerException("User role is null"));

        return Jwts.builder()
                .signWith(getSecretKey(), Jwts.SIG.HS512)
                .subject(subject)
                .issuedAt(date)
                .claim(USER_ID_CLAIM_KEY, userId)
                .claim(ROLENAME_CLAIM_KEY, roleName)
                .expiration(new Date(date.getTime() + getExpiration()))
                .compact();
    }

    /**
     * Проверяет валидность токена.
     * @param token JWT токен
     * @return true если токен валиден, false в противном случае
     */
    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.error("Token expired: {}", expEx.getMessage());
        } catch (UnsupportedJwtException unsEx) {
            log.error("Unsupported jwt: {}", unsEx.getMessage());
        } catch (MalformedJwtException mjEx) {
            log.error("Malformed jwt: {}", mjEx.getMessage());
        } catch (Exception e) {
            log.error("Invalid token: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Возвращает секретный ключ для подписи токенов.
     * @return SecretKey
     */
    @Override
    public SecretKey getSecretKey() {
        byte[] encodeKey = Base64.getDecoder().decode(accessSecret);
        return Keys.hmacShaKeyFor(encodeKey);
    }

    /**
     * Возвращает время жизни токена в миллисекундах.
     * @return время в ms
     */
    @Override
    public Long getExpiration() {
        return Long.parseLong(accessExpirationTime);
    }

    /**
     * Извлекает claims (утверждения) из токена.
     * @param token JWT токен
     * @return объект Claims
     */
    @Override
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Создает объект аутентификации на основе claims токена.
     * @param claims утверждения из токена
     * @return объект JwtAuthentication
     */
    @Override
    public JwtAuthentication authenticate(Claims claims) {
        final JwtAuthentication jwtInfoToken = new JwtAuthentication();
        jwtInfoToken.setUsername(claims.getSubject());
        jwtInfoToken.setRoles(List.of(Role.builder()
                .name((String) claims.get(ROLENAME_CLAIM_KEY))
                .build()));
        jwtInfoToken.setUserId(UUID.fromString(claims.get(USER_ID_CLAIM_KEY, String.class)));
        return jwtInfoToken;
    }
}