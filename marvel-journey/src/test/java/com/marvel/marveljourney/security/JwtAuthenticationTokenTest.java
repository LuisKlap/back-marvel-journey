package com.marvel.marveljourney.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationTokenTest {

    private Claims claims;
    private JwtAuthenticationToken jwtAuthenticationToken;

    @BeforeEach
    void setUp() {
        claims = new DefaultClaims();
        claims.setSubject("testUser");
        claims.put("roles", List.of("ROLE_USER", "ROLE_ADMIN"));
        jwtAuthenticationToken = new JwtAuthenticationToken(claims);
    }

    @Test
    void testGetCredentials() {
        assertEquals(claims, jwtAuthenticationToken.getCredentials());
    }

    @Test
    void testGetPrincipal() {
        assertEquals("testUser", jwtAuthenticationToken.getPrincipal());
    }

    @Test
    void testGetAuthorities() {
        Collection<GrantedAuthority> authorities = jwtAuthenticationToken.getAuthorities();
        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        assertTrue(authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testGetAuthoritiesWhenNoRoles() {
        claims.remove("roles");
        jwtAuthenticationToken = new JwtAuthenticationToken(claims);
        Collection<GrantedAuthority> authorities = jwtAuthenticationToken.getAuthorities();
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }
}