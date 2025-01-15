package com.marvel.marveljourney.util;

import com.marvel.marveljourney.security.KeyManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
        String token = jwtUtil.generateToken("testUser", 1000L, "issuer", "audience");
        assertNotNull(token);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
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
                .setExpiration(new Date(System.currentTimeMillis() + 1000L))
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
    void testParseToken_Invalid() {
        Key invalidKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String token = Jwts.builder()
                .setSubject("testUser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L))
                .setIssuer("issuer")
                .setAudience("audience")
                .signWith(invalidKey, SignatureAlgorithm.HS512)
                .compact();

        Claims claims = jwtUtil.parseToken(token, "issuer", "audience");
        assertNull(claims);
    }
}