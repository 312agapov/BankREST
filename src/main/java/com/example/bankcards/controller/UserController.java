package com.example.bankcards.controller;

import com.example.bankcards.dto.DataDto;
import com.example.bankcards.dto.JwtAccess;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Контроллер для управления пользователями.
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Регистрация нового пользователя.
     * @param jwtAccess данные для регистрации (логин/пароль)
     * @return зарегистрированный пользователь
     */
    @PostMapping("/register")
    public ResponseEntity<User> addUser(@RequestBody JwtAccess jwtAccess) {
        return ResponseEntity.ok(userService.addUser(jwtAccess));
    }

    /**
     * Получение списка пользователей.
     * @param page номер страницы (опционально)
     * @return пагинированный список пользователей
     */
    @GetMapping
    public ResponseEntity<DataDto<User>> getUsers(@RequestParam(required = false) Integer page) {
        return ResponseEntity.ok(userService.findAllUsers(page));
    }

    /**
     * Получение пользователя по ID.
     * @param userId ID пользователя
     * @return данные пользователя
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable("userId") UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    /**
     * Редактирование пользователя.
     * @param user новые данные пользователя
     * @return обновленные данные
     */
    @PutMapping
    public ResponseEntity<User> editUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.editUser(user));
    }

    /**
     * Удаление пользователя.
     * @param userId ID пользователя
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") UUID userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.ok().build();
    }
}
