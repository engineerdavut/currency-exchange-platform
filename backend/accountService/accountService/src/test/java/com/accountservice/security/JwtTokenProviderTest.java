package com.accountservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private SecretKey secretKey;
    
    @BeforeEach
    void setUp() {
        // Create a test secret key
        secretKey = Keys.hmacShaKeyFor("testSecretKeyThatIsAtLeast32BytesLongForHS256".getBytes(StandardCharsets.UTF_8));
        jwtTokenProvider = new JwtTokenProvider(secretKey);
        
        // Set expiration time using reflection
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 3600000L); // 1 hour
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // Arrange
        String username = "testUser";

        // Act
        String token = jwtTokenProvider.generateToken(username);

        // Assert
        assertNotNull(token);
        assertTrue(token.contains(".")); // JWT format has at least one dot
    }

    @Test
    void getUsernameFromToken_ShouldExtractCorrectUsername() {
        // Arrange
        String username = "testUser";
        String token = jwtTokenProvider.generateToken(username);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtTokenProvider.generateToken("testUser");

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() throws Exception {
        // Arrange - Create a token that's already expired
        String username = "testUser";
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() - 1000); // 1 second ago
        
        String expiredToken = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        // Act
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithInvalidSignature_ShouldReturnFalse() {
        // Arrange - Create a token with a different key
        SecretKey differentKey = Keys.hmacShaKeyFor("differentSecretKeyThatIsAtLeast32BytesLong".getBytes(StandardCharsets.UTF_8));
        String username = "testUser";
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000);
        
        String tokenWithDifferentSignature = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(differentKey, SignatureAlgorithm.HS256)
                .compact();

        // Act
        boolean isValid = jwtTokenProvider.validateToken(tokenWithDifferentSignature);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        // Arrange
        String malformedToken = "not.a.valid.jwt.token";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }
}

