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
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"unused"})
public class JwtServiceImpl implements JwtService {

    private static final String USER_ID_CLAIM_KEY = "userId";
    private static final String ROLENAME_CLAIM_KEY = "roleName";

    @Value("${jwt.access.secret}")
    protected String accessSecret;

    @Value("${jwt.access.exp_time}")
    protected String accessExpirationTime;

    @Override
    public String generateToken(String subject, @Nullable User user) {
        Date date = new Date();

        UUID userId;
        String roleName;
        if (user.getId() != null){
            userId = user.getId();
        } else {
            throw new NullPointerException("ID юзера равен null");
        }
        if (user.getRole() != null){
            roleName = user.getRole().getName();
        } else {
            throw new NullPointerException("У пользователя нет роли!");
        }

        JwtBuilder builder = Jwts.builder()
                .signWith(getSecretKey(), Jwts.SIG.HS512)
                .subject(subject)
                .issuedAt(date)
                .claim(USER_ID_CLAIM_KEY, userId)
                .claim(ROLENAME_CLAIM_KEY, roleName)
                .expiration(new Date(date.getTime() + getExpiration()));

        return builder.compact();
    }

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

    @Override
    public SecretKey getSecretKey() {
        byte[] encodeKey = Base64.getDecoder().decode(accessSecret);
        return Keys.hmacShaKeyFor(encodeKey);
    }

    @Override
    public Long getExpiration() {
        return Long.parseLong(accessExpirationTime);
    }

    @Override
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

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