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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Value("${app.pagination.limit}")
    private int paginationLimit;

    public User addUser(JwtAccess jwtAccess) {
        if (StringUtils.isEmpty(jwtAccess.getUsername()) || StringUtils.isEmpty(jwtAccess.getPassword())) {
            throw new IllegalStateException("Введены неполные данные пользователя!");
        } else {
            if (userRepository.findByUsername(jwtAccess.getUsername()).isPresent()) {
                throw new IllegalStateException("Пользователь с таким username уже существует!");
            }
            User user = new User();
            user.setUsername(jwtAccess.getUsername());

            Optional<Role> roleIfPresent = roleRepository.findByName(Constants.Roles.ROLE_USER_CODE);

            user.setPassword(passwordEncoder.encode(jwtAccess.getPassword()));
            user.setRole(roleIfPresent.orElseThrow(() -> new NoSuchElementException("Произошла ошибка, обратитесь к администратору")));
            return userRepository.save(user);
        }
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Пользователь не был найден в БД!"));
    }

    public void deleteUserById(UUID id) {
        userRepository.deleteById(id);
    }

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

        updatedUser.setPassword(existingUser.getPassword());
        updatedUser.setRole(existingUser.getRole());
        updatedUser.setCards(existingUser.getCards());

        return userRepository.save(updatedUser);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

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