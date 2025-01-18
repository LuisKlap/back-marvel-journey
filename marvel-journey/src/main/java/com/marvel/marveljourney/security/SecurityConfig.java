package com.marvel.marveljourney.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private static final String[] PUBLIC_URLS = {
           "/auth/parse-token", "/auth/register", "/auth/login", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
    };

    private static final String[] ADMIN_URLS = { "/admin/**" };
    private static final String[] USER_URLS = { "/user/**" };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        logger.debug("Configurando SecurityFilterChain");
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers(ADMIN_URLS).hasRole("ADMIN")
                        .requestMatchers(USER_URLS).hasRole("USER")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        .defaultsDisabled()
                        .cacheControl(cache -> cache.disable())
                        .contentTypeOptions(contentType -> contentType.disable())
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                        .xssProtection(xss -> xss.disable()));
        logger.debug("SecurityFilterChain configurado com sucesso");
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        logger.debug("Configurando BCryptPasswordEncoder");
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        logger.debug("Configurando CorsConfigurationSource");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        logger.debug("CorsConfigurationSource configurado com sucesso");
        return source;
    }
}