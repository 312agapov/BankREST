package com.example.bankcards.controller;

import com.example.bankcards.dto.DataDto;
import com.example.bankcards.dto.JwtAccess;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private Role mockRole;
    private User createMockUser(UUID id) {
        User user = new User();
        user.setId(id);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(mockRole);
        return user;
    }

    @BeforeEach
    void setUp() {
        mockRole = Mockito.mock(Role.class);
        Mockito.when(mockRole.getName()).thenReturn("ROLE_USER");
    }

    @Test
    void registerUser_ValidRequest_ReturnsOk() throws Exception {
        JwtAccess request = new JwtAccess("testuser", "password");
        User mockUser = createMockUser(UUID.randomUUID());

        Mockito.when(userService.addUser(any(JwtAccess.class))).thenReturn(mockUser);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.role.name").value("ROLE_USER"));
    }

    @Test
    void getUsers_WithPagination_ReturnsOk() throws Exception {
        int page = 1;
        User mockUser = createMockUser(UUID.randomUUID());

        Mockito.when(userService.findAllUsers(page))
                .thenReturn(new DataDto<>(List.of(mockUser), 1, 1));

        mockMvc.perform(get("/api/users?page={page}", page))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].role.name").value("ROLE_USER"));
    }

    @Test
    void getUsers_WithoutPagination_ReturnsOk() throws Exception {
        User mockUser = createMockUser(UUID.randomUUID());

        Mockito.when(userService.findAllUsers(null))
                .thenReturn(new DataDto<>(List.of(mockUser), 1, 1));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].role.name").value("ROLE_USER"));
    }

    @Test
    void getUserById_Exists_ReturnsOk() throws Exception {
        UUID userId = UUID.randomUUID();
        User mockUser = createMockUser(userId);

        Mockito.when(userService.getUserById(userId)).thenReturn(mockUser);

        mockMvc.perform(get("/api/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.role.name").value("ROLE_USER"));
    }

    @Test
    void editUser_ValidRequest_ReturnsOk() throws Exception {
        UUID userId = UUID.randomUUID();

        User requestUser = new User();
        requestUser.setId(userId);
        requestUser.setUsername("updatedUser");

        User mockUser = createMockUser(userId);
        mockUser.setUsername("updatedUser");

        Mockito.when(userService.editUser(any(User.class))).thenReturn(mockUser);

        ObjectMapper testMapper = new ObjectMapper();
        testMapper.addMixIn(User.class, UserMixin.class);

        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testMapper.writeValueAsString(requestUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("updatedUser"))
                .andExpect(jsonPath("$.role.name").value("ROLE_USER"));
    }

    @JsonIgnoreProperties(value = {"authorities", "accountNonExpired",
            "accountNonLocked", "credentialsNonExpired",
            "enabled", "password"}, ignoreUnknown = true)
    private abstract static class UserMixin {}

    @Test
    void deleteUser_ValidId_ReturnsOk() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/users/{userId}", userId))
                .andExpect(status().isOk());

        Mockito.verify(userService).deleteUserById(userId);
    }
}