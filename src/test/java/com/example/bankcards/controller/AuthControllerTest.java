package com.example.bankcards.controller;

import com.example.bankcards.dto.JwtAccess;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.security.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void whenValidCredentials_thenReturnJwtToken() throws Exception {
        JwtAccess request = new JwtAccess("testuser", "testpass");
        JwtResponse response = new JwtResponse("mocked-jwt-token", "testuser");

        when(authService.generateJwtToken(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void whenInvalidCredentials_thenReturnError() throws Exception {
        JwtAccess request = new JwtAccess("wronguser", "wrongpass");

        when(authService.generateJwtToken(any()))
                .thenThrow(new RuntimeException("Пользователь не найден"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Пользователь не найден"));
    }
}

