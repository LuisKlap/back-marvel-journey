package com.marvel.marveljourney.util;

import com.marvel.marveljourney.security.KeyManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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
        String subject = "testUser";
        long expirationTime = 1000 * 60 * 60;
        String issuer = "testIssuer";
        String audience = "testAudience";

        String token = jwtUtil.generateToken(subject, expirationTime, issuer, audience);

        assertNotNull(token);
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        assertEquals(subject, claims.getSubject());
        assertEquals(issuer, claims.getIssuer());
        assertEquals(audience, claims.getAudience());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void testParseToken() {
        String subject = "testUser";
        long expirationTime = 1000 * 60 * 60;
        String issuer = "testIssuer";
        String audience = "testAudience";

        String token = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .setIssuer(issuer)
                .setAudience(audience)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        Claims claims = jwtUtil.parseToken(token, issuer, audience);

        assertNotNull(claims);
        assertEquals(subject, claims.getSubject());
        assertEquals(issuer, claims.getIssuer());
        assertEquals(audience, claims.getAudience());
    }

    @Test
    void testParseTokenInvalidIssuer() {
        String subject = "testUser";
        long expirationTime = 1000 * 60 * 60;
        String issuer = "testIssuer";
        String audience = "testAudience";

        String token = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .setIssuer(issuer)
                .setAudience(audience)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        Claims claims = jwtUtil.parseToken(token, "invalidIssuer", audience);

        assertNull(claims);
    }

    @Test
    void testParseTokenInvalidAudience() {
        String subject = "testUser";
        long expirationTime = 1000 * 60 * 60;
        String issuer = "testIssuer";
        String audience = "testAudience";

        String token = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .setIssuer(issuer)
                .setAudience(audience)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        Claims claims = jwtUtil.parseToken(token, issuer, "invalidAudience");

        assertNull(claims);
    }
}