package com.example.bankcards.service;

import com.example.bankcards.config.Constants;
import com.example.bankcards.dto.DataDto;
import com.example.bankcards.dto.JwtAccess;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.PageMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Сервис для работы с пользователями системы.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Value("${app.pagination.limit}")
    private int paginationLimit;

    /**
     * Регистрирует нового пользователя.
     * @param jwtAccess данные для регистрации (логин и пароль)
     * @return зарегистрированный пользователь
     * @throws IllegalStateException если:
     *         - данные неполные
     *         - пользователь уже существует
     * @throws NoSuchElementException если роль USER не найдена
     */
    @Transactional
    public User addUser(JwtAccess jwtAccess) {
        if (StringUtils.isEmpty(jwtAccess.getUsername()) || StringUtils.isEmpty(jwtAccess.getPassword())) {
            throw new IllegalStateException("Введены неполные данные пользователя!");
        }

        if (userRepository.findByUsername(jwtAccess.getUsername()).isPresent()) {
            throw new IllegalStateException("Пользователь с таким username уже существует!");
        }

        User user = new User();
        user.setUsername(jwtAccess.getUsername());
        user.setPassword(passwordEncoder.encode(jwtAccess.getPassword()));

        Role userRole = roleRepository.findByName(Constants.Roles.ROLE_USER_CODE)
                .orElseThrow(() -> new NoSuchElementException("Произошла ошибка, обратитесь к администратору"));
        user.setRole(userRole);

        return userRepository.save(user);
    }

    /**
     * Находит пользователя по ID.
     * @param id UUID пользователя
     * @return найденный пользователь
     * @throws IllegalStateException если пользователь не найден
     */
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Пользователь не был найден в БД!"));
    }

    /**
     * Удаляет пользователя по ID.
     * @param id UUID пользователя
     */
    @Transactional
    public void deleteUserById(UUID id) {
        userRepository.deleteById(id);
    }

    /**
     * Обновляет данные пользователя.
     * @param updatedUser новые данные пользователя
     * @return обновленный пользователь
     * @throws NoSuchElementException если пользователь не найден
     * @throws IllegalArgumentException если username уже занят
     */
    @Transactional
    public User editUser(User updatedUser) {
        User existingUser = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        if (updatedUser.getUsername() != null
                && !updatedUser.getUsername().isBlank()
                && !existingUser.getUsername().equals(updatedUser.getUsername())) {

            if (userRepository.existsByUsername(updatedUser.getUsername())) {
                throw new IllegalArgumentException("Username уже занят");
            }
            existingUser.setUsername(updatedUser.getUsername());
        }

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    /**
     * Загружает пользователя по username для Spring Security.
     * @param username логин пользователя
     * @return UserDetails
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    /**
     * Возвращает список пользователей с пагинацией.
     * @param page номер страницы (начиная с 1), null - все пользователи
     * @return список пользователей с метаданными пагинации
     */
    public DataDto<User> findAllUsers(Integer page) {
        if (page != null) {
            int actualPage = page - 1;
            return PageMapper.mapToDataDto(
                    userRepository.findAll(PageRequest.of(actualPage, paginationLimit))
            );
        }

        List<User> users = userRepository.findAll();
        return DataDto.<User>builder()
                .data(users)
                .totalPages(1)
                .totalElements(users.size())
                .build();
    }
}