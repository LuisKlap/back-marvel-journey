package com.marvel.marveljourney.util;

import com.marvel.marveljourney.security.KeyManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.crypto.SecretKey;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilTest {

    @Mock
    private KeyManager keyManager;

    @InjectMocks
    private JwtUtil jwtUtil;

    private SecretKey key;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512); // Chave de 512 bits
        when(keyManager.getActiveKey()).thenReturn(key);
    }

    @Test
    void testGenerateToken() {
        String subject = "testUser";
        long expirationTime = 1000 * 60 * 60;
        String issuer = "testIssuer";
        String audience = "testAudience";
        List<String> roles = List.of("ROLE_USER");

        String token = jwtUtil.generateToken(subject, expirationTime, issuer, audience, roles);

        assertNotNull(token);
    }

    @Test
    void testParseToken() {
        String subject = "testUser";
        long expirationTime = 1000 * 60 * 60;
        String issuer = "testIssuer";
        String audience = "testAudience";
        List<String> roles = List.of("ROLE_USER");

        String token = jwtUtil.generateToken(subject, expirationTime, issuer, audience, roles);

        Claims claims = jwtUtil.parseToken(token, issuer, audience);

        assertNotNull(claims);
        assertEquals(subject, claims.getSubject());
        assertEquals(issuer, claims.getIssuer());
        assertEquals(audience, claims.getAudience());
        assertEquals(roles, claims.get("roles"));
    }

    @Test
    void testParseTokenWithInvalidIssuer() {
        String subject = "testUser";
        long expirationTime = 1000 * 60 * 60;
        String issuer = "testIssuer";
        String audience = "testAudience";
        List<String> roles = List.of("ROLE_USER");

        String token = jwtUtil.generateToken(subject, expirationTime, issuer, audience, roles);

        assertThrows(JwtException.class, () -> jwtUtil.parseToken(token, "invalidIssuer", audience));
    }

    @Test
    void testParseTokenWithInvalidAudience() {
        String subject = "testUser";
        long expirationTime = 1000 * 60 * 60;
        String issuer = "testIssuer";
        String audience = "testAudience";
        List<String> roles = List.of("ROLE_USER");

        String token = jwtUtil.generateToken(subject, expirationTime, issuer, audience, roles);

        assertThrows(JwtException.class, () -> jwtUtil.parseToken(token, issuer, "invalidAudience"));
    }

    @Test
    void testParseTokenWithExpiredToken() {
        String subject = "testUser";
        long expirationTime = -1000 * 60 * 60;
        String issuer = "testIssuer";
        String audience = "testAudience";
        List<String> roles = List.of("ROLE_USER");

        String token = jwtUtil.generateToken(subject, expirationTime, issuer, audience, roles);

        assertThrows(JwtException.class, () -> jwtUtil.parseToken(token, issuer, audience));
    }
}