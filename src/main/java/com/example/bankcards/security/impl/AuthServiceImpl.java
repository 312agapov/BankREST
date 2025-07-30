package com.example.bankcards.security.impl;

import com.example.bankcards.dto.JwtAccess;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.AuthService;
import com.example.bankcards.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    protected final JwtService jwtService;
    protected final PasswordEncoder passwordEncoder;
    protected final UserRepository userRepository;
    protected final AuthenticationManager authenticationManager;

    @Override
    public JwtResponse generateJwtToken(JwtAccess jwtAccess) {
        Optional<User> user = userRepository.findByUsername(jwtAccess.getUsername());

        if (user.isPresent()) {
            if (!passwordEncoder.matches(jwtAccess.getPassword(), user.get().getPassword()))
                throw new RuntimeException("Неверный пароль");
        } else {
            throw new RuntimeException("Пользователь не найден");
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.get().getUsername(), jwtAccess.getPassword()));

        return new JwtResponse(jwtService.generateToken(user.get().getUsername(),user.get()),
                user.get().getUsername());
    }
}
