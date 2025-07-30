package com.example.bankcards.security;

import com.example.bankcards.dto.JwtAccess;
import com.example.bankcards.dto.JwtResponse;

public interface AuthService {
    JwtResponse generateJwtToken(JwtAccess jwtReq);
}
