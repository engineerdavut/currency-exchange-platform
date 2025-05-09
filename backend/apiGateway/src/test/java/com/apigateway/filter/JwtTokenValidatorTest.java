package com.apigateway.filter;

import com.apigateway.exception.InvalidTokenException;
import com.apigateway.exception.TokenExpiredException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class) // Mockito kullanmasak da JUnit 5 için
class JwtTokenValidatorTest {

    private JwtTokenValidator jwtTokenValidator;
    private SecretKey testSecretKey;
    private final String TEST_USERNAME = "testuser";
    private final String JWT_COOKIE_NAME = "jwt";

    @BeforeEach
    void setUp() {
        // Her test için aynı, bilinen bir anahtar kullan
        String secretString = "testSecretKeyThatIsAtLeast32BytesLongForHS256ApiGateway";
        testSecretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
        jwtTokenValidator = new JwtTokenValidator(testSecretKey);
    }

    // Helper method to generate a test token
    private String generateTestToken(String username, Instant expirationTime) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expirationTime))
                .signWith(testSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void validateAndExtractUser_WithValidToken_ShouldReturnUsername() {
        // Arrange
        String validToken = generateTestToken(TEST_USERNAME, Instant.now().plus(1, ChronoUnit.HOURS));
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/some/protected/path")
                .cookie(new HttpCookie(JWT_COOKIE_NAME, validToken))
                .build();

        // Act
        String extractedUsername = jwtTokenValidator.validateAndExtractUser(request);

        // Assert
        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    void validateAndExtractUser_WithExpiredToken_ShouldThrowTokenExpiredException() {
        // Arrange
        String expiredToken = generateTestToken(TEST_USERNAME, Instant.now().minus(1, ChronoUnit.MINUTES));
         MockServerHttpRequest request = MockServerHttpRequest.get("/api/some/protected/path")
                .cookie(new HttpCookie(JWT_COOKIE_NAME, expiredToken))
                .build();

        // Act & Assert
        assertThrows(TokenExpiredException.class, () -> {
            jwtTokenValidator.validateAndExtractUser(request);
        });
    }

     @Test
    void validateAndExtractUser_WithInvalidSignature_ShouldThrowInvalidTokenException() {
        // Arrange
        // Farklı bir anahtarla token oluştur
        SecretKey wrongKey = Keys.hmacShaKeyFor("anotherSecretKeyThatIsDifferentAndAlsoLongEnough".getBytes(StandardCharsets.UTF_8));
        String tokenWithWrongSignature = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(wrongKey, SignatureAlgorithm.HS256)
                .compact();

         MockServerHttpRequest request = MockServerHttpRequest.get("/api/some/protected/path")
                .cookie(new HttpCookie(JWT_COOKIE_NAME, tokenWithWrongSignature))
                .build();

        // Act & Assert
         // Jwts library might throw different specific exceptions, but we expect our validator to wrap them or throw InvalidTokenException
         assertThrows(InvalidTokenException.class, () -> {
             jwtTokenValidator.validateAndExtractUser(request);
         }, "Expected InvalidTokenException for wrong signature");
    }

     @Test
    void validateAndExtractUser_WithMalformedToken_ShouldThrowInvalidTokenException() {
        // Arrange
        String malformedToken = "this.is.not.a.jwt";
         MockServerHttpRequest request = MockServerHttpRequest.get("/api/some/protected/path")
                .cookie(new HttpCookie(JWT_COOKIE_NAME, malformedToken))
                .build();

        // Act & Assert
         assertThrows(InvalidTokenException.class, () -> {
             jwtTokenValidator.validateAndExtractUser(request);
         });
    }

     @Test
    void validateAndExtractUser_WithMissingCookie_ShouldReturnNull() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/some/protected/path")
                .build(); // No cookie

        // Act
        String extractedUsername = jwtTokenValidator.validateAndExtractUser(request);

        // Assert
        assertNull(extractedUsername);
    }

    @Test
    void validateAndExtractUser_WithEmptyCookieValue_ShouldThrowInvalidTokenException() {
        // Arrange
         MockServerHttpRequest request = MockServerHttpRequest.get("/api/some/protected/path")
                .cookie(new HttpCookie(JWT_COOKIE_NAME, "")) // Empty value
                .build();

        // Act & Assert
         assertThrows(InvalidTokenException.class, () -> {
             jwtTokenValidator.validateAndExtractUser(request);
         });
    }
}