package com.example.bankcards.security.impl;

import com.example.bankcards.entity.Role;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class JwtAuthentication implements Authentication {

    private boolean authenticated;
    private String username;
    private UUID userId;
    private List<Role> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return roles; }

    @Override
    public Object getCredentials() { return null; }

    @Override
    public Object getDetails() { return null; }

    @Override
    public Object getPrincipal() { return username; }

    @Override
    public boolean isAuthenticated() { return authenticated; }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return username;
    }
}