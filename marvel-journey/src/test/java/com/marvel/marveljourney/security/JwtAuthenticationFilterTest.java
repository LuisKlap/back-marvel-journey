package com.marvel.marveljourney.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.marvel.marveljourney.util.JwtUtil;

import javax.servlet.ServletException;
import java.io.IOException;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private jakarta.servlet.FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    @Test
    void testInvalidToken() throws ServletException, IOException, jakarta.servlet.ServletException {
        request.addHeader("Authorization", "Bearer invalidtoken");

        when(jwtUtil.parseToken(anyString(), anyString(), anyString())).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testExpiredToken() throws ServletException, IOException, jakarta.servlet.ServletException {
        String expiredToken = Jwts.builder()
                .setSubject("user")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000))
                .setExpiration(new Date(System.currentTimeMillis() - 500))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        request.addHeader("Authorization", "Bearer " + expiredToken);

        when(jwtUtil.parseToken(anyString(), anyString(), anyString())).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testValidToken() throws ServletException, IOException, jakarta.servlet.ServletException {
        String validToken = Jwts.builder()
                .setSubject("user")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        request.addHeader("Authorization", "Bearer " + validToken);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(validToken)
                .getBody();

        when(jwtUtil.parseToken(anyString(), anyString(), anyString())).thenReturn(claims);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user", ((Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject());
        verify(filterChain).doFilter(request, response);
    }
}