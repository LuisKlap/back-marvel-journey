package com.marvel.marveljourney.util;

import com.marvel.marveljourney.security.KeyManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Clock;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClock;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Autowired
    private KeyManager keyManager;

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final String ISSUER_AUDIENCE_MISMATCH = "Issuer or Audience does not match";
    private static final Clock clock = DefaultClock.INSTANCE;
    private static final long ALLOWED_CLOCK_SKEW_MILLIS = 30000; // 30 segundos de tolerância

    public String generateToken(String subject, long expirationTime, String issuer, String audience, List<String> roles) {
        try {
            Key key = keyManager.getActiveKey();
            logger.info("Chave ativa na geração do token: {}", Base64.getEncoder().encodeToString(key.getEncoded()));
            String token = Jwts.builder()
                    .setSubject(subject)
                    .setIssuedAt(clock.now())
                    .setExpiration(new Date(clock.now().getTime() + expirationTime))
                    .setIssuer(issuer)
                    .setAudience(audience)
                    .claim("roles", roles)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();
            logger.info("Token JWT gerado com sucesso para o sujeito: {}", subject);
            return token;
        } catch (Exception e) {
            logger.error("Erro ao gerar token JWT para o sujeito: {}", subject, e);
            throw new RuntimeException("Erro ao gerar token JWT", e);
        }
    }

        public Claims parseToken(String token, String expectedIssuer, String expectedAudience) {
        Key key = keyManager.getActiveKey();
        logger.info("Chave ativa no parse do token: {}", Base64.getEncoder().encodeToString(key.getEncoded()));
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(ALLOWED_CLOCK_SKEW_MILLIS / 1000)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
    
            if (isIssuerAndAudienceValid(claims, expectedIssuer, expectedAudience)) {
                logger.info("Token JWT válido para o sujeito: {}", claims.getSubject());
                return claims;
            } else {
                logger.warn(ISSUER_AUDIENCE_MISMATCH);
                throw new JwtException(ISSUER_AUDIENCE_MISMATCH);
            }
        } catch (JwtException e) {
            logger.error("Erro ao analisar o token JWT", e);
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao analisar o token JWT", e);
            throw new RuntimeException("Erro inesperado ao analisar o token JWT", e);
        }
    }

    private boolean isIssuerAndAudienceValid(Claims claims, String expectedIssuer, String expectedAudience) {
        return expectedIssuer.equals(claims.getIssuer()) && expectedAudience.equals(claims.getAudience());
    }
}