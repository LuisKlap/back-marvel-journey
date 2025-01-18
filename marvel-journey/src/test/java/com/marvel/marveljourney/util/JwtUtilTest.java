package com.marvel.marveljourney.util;

import com.marvel.marveljourney.security.KeyManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilTest {

    @Mock
    private KeyManager keyManager;

    @InjectMocks
    private JwtUtil jwtUtil;

    private Key key;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        when(keyManager.getActiveKey()).thenReturn(key);
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken("testUser", 10000L, "issuer", "audience"); // Ajuste o tempo de expiração para 10 segundos
        assertNotNull(token);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(60) // Adiciona tolerância de tempo de 60 segundos
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("testUser", claims.getSubject());
        assertEquals("issuer", claims.getIssuer());
        assertEquals("audience", claims.getAudience());
    }

    @Test
    void testParseToken_Valid() {
        String token = Jwts.builder()
                .setSubject("testUser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 10000L)) // Ajuste o tempo de expiração para 10 segundos
                .setIssuer("issuer")
                .setAudience("audience")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        Claims claims = jwtUtil.parseToken(token, "issuer", "audience");
        assertNotNull(claims);
        assertEquals("testUser", claims.getSubject());
        assertEquals("issuer", claims.getIssuer());
        assertEquals("audience", claims.getAudience());
    }

        @Test
    void testParseToken_InvalidSignature() {
        Key invalidKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String token = Jwts.builder()
                .setSubject("testUser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 10000L)) // Ajuste o tempo de expiração para 10 segundos
                .setIssuer("issuer")
                .setAudience("audience")
                .signWith(invalidKey, SignatureAlgorithm.HS512)
                .compact();
    
        JwtException exception = assertThrows(JwtException.class, () -> {
            jwtUtil.parseToken(token, "issuer", "audience");
        });
    
        assertEquals("JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.", exception.getMessage());
    }

    @Test
    void testParseToken_InvalidIssuerOrAudience() {
        String token = Jwts.builder()
                .setSubject("testUser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 10000L)) // Ajuste o tempo de expiração para 10 segundos
                .setIssuer("invalidIssuer")
                .setAudience("invalidAudience")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        JwtException exception = assertThrows(JwtException.class, () -> {
            jwtUtil.parseToken(token, "issuer", "audience");
        });

        assertEquals("Issuer or Audience does not match", exception.getMessage());
    }
}