package com.accountservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.SecureDigestAlgorithm;

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

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    public JwtTokenProvider(SecretKey key) {
        this.key = key;
        logger.debug("AccountService JwtTokenProvider initialized with SecretKey object hash: {}",
                System.identityHashCode(key));
    }

    public String generateToken(String username) {
        logger.debug("[JwtTokenProvider] generateToken: username={}", username);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        SecureDigestAlgorithm<SecretKey, SecretKey> signatureAlgorithm = Jwts.SIG.HS256;

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, signatureAlgorithm)
                .compact();

        String tokenStart = token.substring(0, Math.min(token.length(), 15));
        logger.debug("Generated JWT for user '{}', starts with: {}...", username, tokenStart);

        return token;
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            throw e;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            logger.debug("[JwtTokenProvider] validateToken: valid");
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired and not valid: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature during validation: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token during validation: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token during validation: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty during validation: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[JwtTokenProvider] validateToken: invalid due to an unexpected error", e);
        }
        return false;
    }
}
