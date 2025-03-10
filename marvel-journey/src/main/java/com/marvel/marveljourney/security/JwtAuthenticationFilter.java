package com.marvel.marveljourney.security;

import com.marvel.marveljourney.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    private static final String ISSUER = "seu-servidor";
    private static final String AUDIENCE = "seu-aplicativo";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        logger.debug("Cabeçalho de autorização: {}", header);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            logger.debug("Token extraído: {}", token);

            Claims claims = null;
            try {
                claims = jwtUtil.parseToken(token, ISSUER, AUDIENCE);
                logger.debug("Claims extraídas: {}", claims);
            } catch (Exception e) {
                logger.error("Erro ao analisar o token JWT", e);
            }

            if (claims != null) {
                String username = claims.getSubject();
                if (username != null) {
                    UserDetails userDetails = new User(username, "", List.of(new SimpleGrantedAuthority("ROLE_USER")));
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else {
                logger.warn("Token inválido");
                SecurityContextHolder.clearContext();
            }
        } else {
            logger.warn("Cabeçalho de autorização ausente ou malformado");
        }
        chain.doFilter(request, response);
    }
}