package com.marvel.marveljourney.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import io.jsonwebtoken.Claims;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Claims claims;

    public JwtAuthenticationToken(Claims claims) {
        super(null);
        this.claims = claims;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
            return claims;
    }

    @Override
    public Object getPrincipal() {
            return claims;
    }
}