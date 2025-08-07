package com.example.bankcards.controller;

import com.example.bankcards.dto.JwtAccess;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для аутентификации.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Аутентификация пользователя и получение JWT-токена.
     * @param jwtAccess логин и пароль пользователя
     * @return JWT-токен и имя пользователя
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> auth(@RequestBody JwtAccess jwtAccess) {
        return ResponseEntity.ok(authService.generateJwtToken(jwtAccess));
    }
}