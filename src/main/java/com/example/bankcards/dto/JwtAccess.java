package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class JwtAccess {
    protected String username;
    protected String password;
}
