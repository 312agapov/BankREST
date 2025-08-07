package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Запрос аутентификации.
 */
@Data
@AllArgsConstructor
public class JwtAccess {
    /** Логин */
    private String username;
    /** Пароль */
    private String password;
}
