package com.example.bankcards.controller;

import com.example.bankcards.dto.JwtAccess;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.security.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> auth(@RequestBody JwtAccess jwtAccess, HttpServletRequest request) {

        JwtResponse response = authService.generateJwtToken(jwtAccess);
        return ResponseEntity.ok(response);
    }
}