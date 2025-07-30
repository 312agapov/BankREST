package com.example.bankcards.service;

import com.example.bankcards.config.Constants;
import com.example.bankcards.dto.DataDto;
import com.example.bankcards.dto.JwtAccess;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    private static final int PAGINATION_LIMIT = 5;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(userService, "paginationLimit", PAGINATION_LIMIT);
    }

    @Test
    void addUser_ValidData_ReturnsSavedUser() {
        // Arrange
        JwtAccess access = new JwtAccess("testuser", "password");
        Role role = new Role(UUID.randomUUID(), Constants.Roles.ROLE_USER_CODE);
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setUsername("testuser");
        savedUser.setRole(role);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(roleRepository.findByName(Constants.Roles.ROLE_USER_CODE)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.addUser(access);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addUser_DuplicateUsername_ThrowsException() {
        JwtAccess access = new JwtAccess("testuser", "password");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(new User()));
        assertThrows(IllegalStateException.class, () -> userService.addUser(access));
    }

    @Test
    void addUser_EmptyUsernameOrPassword_ThrowsException() {
        JwtAccess access = new JwtAccess("", "");
        assertThrows(IllegalStateException.class, () -> userService.addUser(access));
    }

    @Test
    void getUserById_ValidId_ReturnsUser() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.getUserById(id);

        assertEquals(id, result.getId());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> userService.getUserById(id));
    }

    @Test
    void deleteUserById_ValidId_DeletesUser() {
        UUID id = UUID.randomUUID();
        userService.deleteUserById(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    void loadUserByUsername_Found_ReturnsUserDetails() {
        User user = new User();
        user.setUsername("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        var result = userService.loadUserByUsername("admin");
        assertEquals("admin", result.getUsername());
    }

    @Test
    void loadUserByUsername_NotFound_ThrowsException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("ghost"));
    }

    @Test
    void findAllUsers_WithPage_ReturnsPagedData() {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(users));

        DataDto<User> result = userService.findAllUsers(1);

        assertEquals(2, result.getData().size());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void findAllUsers_WithoutPage_ReturnsAll() {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        DataDto<User> result = userService.findAllUsers(null);
        assertEquals(2, result.getData().size());
        assertEquals(1, result.getTotalPages());
    }
}
