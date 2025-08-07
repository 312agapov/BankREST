package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Ответ с JWT-токеном.
 */
@Data
@AllArgsConstructor
public class JwtResponse {
    /** JWT токен доступа */
    private String accessToken;
    /** Имя пользователя */
    private String username;
}
