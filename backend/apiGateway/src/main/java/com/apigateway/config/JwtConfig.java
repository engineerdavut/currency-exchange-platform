package com.apigateway.config;

import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig { // Veya SecurityConfig içine taşıyabilirsin

    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);

    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public SecretKey jwtSecretKey() {
        // Secret key uzunluğunu kontrol et (HS256 için en az 32 byte)
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            logger.error("API Gateway JWT Secret key is too short! Must be at least 256 bits (32 bytes) long for HS256.");
            throw new IllegalArgumentException("JWT Secret key must be at least 256 bits (32 bytes) long for HS256");
        }
        logger.info("Creating SecretKey bean for API Gateway using the provided secret.");
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}