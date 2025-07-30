package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    protected String accessToken;
    protected String username;
}
