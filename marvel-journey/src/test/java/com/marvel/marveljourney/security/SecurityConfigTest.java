package com.marvel.marveljourney.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import com.marvel.marveljourney.config.EmailConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@SpringBootTest
@Import({SecurityConfig.class, EmailConfig.class})
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testPasswordEncoder() {
        BCryptPasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder, "BCryptPasswordEncoder não deveria ser nulo");
        assertTrue(encoder.matches("password", encoder.encode("password")), "A senha codificada deveria corresponder à senha original");
    }

    @Test
    void testCorsConfigurationSource() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        assertNotNull(source, "CorsConfigurationSource não deveria ser nulo");
    }

    @Test
    void testSecurityFilterChain() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class);
        SecurityFilterChain filterChain = securityConfig.securityFilterChain(http, jwtAuthenticationFilter);
        assertNotNull(filterChain, "SecurityFilterChain não deveria ser nulo");
    }
}