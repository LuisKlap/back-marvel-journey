package com.marvel.marveljourney.util;

import com.marvel.marveljourney.security.KeyManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Autowired
    private KeyManager keyManager;

    private Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final String ISSUER_AUDIENCE_MISMATCH = "Issuer or Audience does not match";

    public String generateToken(String subject, long expirationTime, String issuer, String audience) {
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
        Key key = keyManager.getActiveKey();
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (isIssuerAndAudienceValid(claims, expectedIssuer, expectedAudience)) {
                return claims;
            } else {
                logger.warn(ISSUER_AUDIENCE_MISMATCH);
            }
        } catch (JwtException e) {
            logger.error("Erro ao analisar o token JWT", e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao analisar o token JWT", e);
        }
        return null;
    }

    private boolean isIssuerAndAudienceValid(Claims claims, String expectedIssuer, String expectedAudience) {
        return expectedIssuer.equals(claims.getIssuer()) && expectedAudience.equals(claims.getAudience());
    }
}