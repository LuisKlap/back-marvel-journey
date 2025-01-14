package com.marvel.marveljourney.util;

import com.marvel.marveljourney.security.KeyManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Autowired
    private KeyManager keyManager;

    public String generateToken(String subject, long expirationTime, String issuer, String audience) {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512); // Gera uma chave segura
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .setIssuer(issuer)
                .setAudience(audience)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims parseToken(String token, String expectedIssuer, String expectedAudience) {
        for (Key key : keyManager.getAllKeys()) {
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                if (expectedIssuer.equals(claims.getIssuer()) && expectedAudience.equals(claims.getAudience())) {
                    return claims;
                }
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }
}