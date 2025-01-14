package com.marvel.marveljourney.util;

import com.marvel.marveljourney.security.KeyManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.Key;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilTest {

    @Mock
    private KeyManager keyManager;

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(keyManager.getActiveKey()).thenReturn(key);
        when(keyManager.getAllKeys()).thenReturn(List.of(key));
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken("user", 10000, "issuer", "audience");
        assertNotNull(token);
    }

    @Test
    void testParseToken() {
        String token = jwtUtil.generateToken("user", 10000, "issuer", "audience");
        Claims claims = jwtUtil.parseToken(token, "issuer", "audience");
        assertNotNull(claims);
        assertEquals("user", claims.getSubject());
    }

    @Test
    void testParseTokenInvalidIssuer() {
        String token = jwtUtil.generateToken("user", 10000, "issuer", "audience");
        Claims claims = jwtUtil.parseToken(token, "invalidIssuer", "audience");
        assertNull(claims);
    }

    @Test
    void testParseTokenInvalidAudience() {
        String token = jwtUtil.generateToken("user", 10000, "issuer", "audience");
        Claims claims = jwtUtil.parseToken(token, "issuer", "invalidAudience");
        assertNull(claims);
    }
}