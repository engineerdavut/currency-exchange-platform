package com.accountservice.security;

import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm; // DEPRECATED, kaldırıldı
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm; // Yeni import
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
    private final long expirationTime = 3600000L;

    private SecureDigestAlgorithm<SecretKey, SecretKey> signatureAlgorithm;

    @BeforeEach
    void setUp() {
        String secretString = "testSecretKeyThatIsAtLeast32BytesLongForHS256testtest123";
        secretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));

        signatureAlgorithm = Jwts.SIG.HS256; 

        jwtTokenProvider = new JwtTokenProvider(secretKey);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", expirationTime);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        String username = "testUser";
        String token = jwtTokenProvider.generateToken(username);
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3, "Token should have 3 parts");
    }

    @Test
    void getUsernameFromToken_ShouldExtractCorrectUsername() {
        String username = "testUser";
        String token = jwtTokenProvider.generateToken(username);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        String token = jwtTokenProvider.generateToken("testUser");
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        String username = "testUserExpired";
        Date now = new Date();
        
        Date issuedAt = new Date(now.getTime() - 2 * expirationTime);
        Date expiryDate = new Date(now.getTime() - expirationTime);

        String expiredToken = Jwts.builder()
                .subject(username)
                .issuedAt(issuedAt)
                .expiration(expiryDate)
                .signWith(secretKey, signatureAlgorithm)
                .compact();

        assertFalse(jwtTokenProvider.validateToken(expiredToken));
    }

    @Test
    void validateToken_WithInvalidSignature_ShouldReturnFalse() {
        String differentSecretString = "anotherDifferentSecretKeyForTestingPurposes12345";
        SecretKey differentKey = Keys.hmacShaKeyFor(differentSecretString.getBytes(StandardCharsets.UTF_8));
        String username = "testUserInvalidSig";
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        String tokenWithDifferentSignature = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(differentKey, signatureAlgorithm)
                .compact();

        assertFalse(jwtTokenProvider.validateToken(tokenWithDifferentSignature));
    }

    @Test
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        String malformedToken = "not.a.valid.jwt.token";
        assertFalse(jwtTokenProvider.validateToken(malformedToken));
    }

    @Test
    void validateToken_WithNullToken_ShouldReturnFalse() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }
}
