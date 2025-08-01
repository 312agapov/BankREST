package com.example.bankcards.controller;

import com.example.bankcards.dto.JwtAccess;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> addUser(@RequestBody JwtAccess jwtAccess) {
        return ResponseEntity.ok(userService.addUser(jwtAccess));
    }

    @GetMapping
    public ResponseEntity<?> getUsers(@RequestParam(required = false) Integer page) {
        return ResponseEntity.ok(userService.findAllUsers(page));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable("userId") UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping
    public ResponseEntity<?> editUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.editUser(user));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") UUID userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.ok().build();
    }
}
