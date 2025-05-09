package com.accountservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey key;

    @Value("${JWT_EXPIRATION}")
    private long jwtExpiration;
    
    
    public JwtTokenProvider(SecretKey key) { // Constructor injection doğru
        this.key = key;
        // *** DEBUG LOG: Kullanılan SecretKey objesinin referansını logla ***
        logger.debug("AccountService JwtTokenProvider initialized. Using SecretKey object: {}", System.identityHashCode(key));
    }

    public String generateToken(String username) {
        logger.debug("[JwtTokenProvider] generateToken: username={}", username);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // *** DEBUG LOG: Üretilen token'ın başını logla ***
        String tokenStart = token.substring(0, Math.min(token.length(), 15));
        logger.debug("Generated JWT for user '{}', starts with: {}...", username, tokenStart);

        return token;
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
                    logger.debug("[JwtTokenProvider] validateToken: valid");
            return true;
        } catch (Exception e) {
            logger.warn("[JwtTokenProvider] validateToken: invalid", e);
            return false;
        }
    }

    
}
