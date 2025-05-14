package com.apigateway.filter;

import com.apigateway.exception.InvalidTokenException;
import com.apigateway.exception.TokenExpiredException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm; 
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

@ExtendWith(MockitoExtension.class)
class JwtTokenValidatorTest {

    private JwtTokenValidator jwtTokenValidator;
    private SecretKey testSecretKey;
    private final String TEST_USERNAME = "testuser";
    private final String JWT_COOKIE_NAME = "jwt";
    private SecureDigestAlgorithm<SecretKey, SecretKey> signatureAlgorithm;

    @BeforeEach
    void setUp() {
        String secretString = "testSecretKeyThatIsAtLeast32BytesLongForHS256ApiGateway";
        testSecretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
        jwtTokenValidator = new JwtTokenValidator(testSecretKey);
        signatureAlgorithm = Jwts.SIG.HS256; 
    }

    private String generateTestToken(String username, Instant expirationTime, SecretKey signingKey) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)                
                .issuedAt(Date.from(now))      
                .expiration(Date.from(expirationTime)) 
                .signWith(signingKey, signatureAlgorithm)
                .compact();
    }

    private String generateTestToken(String username, Instant expirationTime) {
        return generateTestToken(username, expirationTime, testSecretKey);
    }


    @Test
    void validateAndExtractUser_WithValidToken_ShouldReturnUsername() {
        String validToken = generateTestToken(TEST_USERNAME, Instant.now().plus(1, ChronoUnit.HOURS));
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/some/protected/path")
                .cookie(new HttpCookie(JWT_COOKIE_NAME, validToken))
                .build();

        String extractedUsername = jwtTokenValidator.validateAndExtractUser(request);

        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    void validateAndExtractUser_WithExpiredToken_ShouldThrowTokenExpiredException() {
        String expiredToken = generateTestToken(TEST_USERNAME, Instant.now().minus(1, ChronoUnit.MINUTES));
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/some/protected/path")
                .cookie(new HttpCookie(JWT_COOKIE_NAME, expiredToken))
                .build();

        assertThrows(TokenExpiredException.class, () -> {
            jwtTokenValidator.validateAndExtractUser(request);
        });
    }

    @Test
    void validateAndExtractUser_WithInvalidSignature_ShouldThrowInvalidTokenException() {
        SecretKey wrongKey = Keys.hmacShaKeyFor("anotherDifferentSecretKeyThatIsAlsoLongEnoughForTest123".getBytes(StandardCharsets.UTF_8));
        String tokenWithWrongSignature = generateTestToken(TEST_USERNAME, Instant.now().plus(1, ChronoUnit.HOURS), wrongKey);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/some/protected/path")
                .cookie(new HttpCookie(JWT_COOKIE_NAME, tokenWithWrongSignature))
                .build();

        assertThrows(InvalidTokenException.class, () -> {
            jwtTokenValidator.validateAndExtractUser(request);
        }, "Expected InvalidTokenException for wrong signature");
    }

    @Test
    void validateAndExtractUser_WithMalformedToken_ShouldThrowInvalidTokenException() {
        String malformedToken = "this.is.not.a.jwt";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/some/protected/path")
                .cookie(new HttpCookie(JWT_COOKIE_NAME, malformedToken))
                .build();

        assertThrows(InvalidTokenException.class, () -> {
            jwtTokenValidator.validateAndExtractUser(request);
        });
    }

    @Test
    void validateAndExtractUser_WithMissingCookie_ShouldReturnNull() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/some/protected/path")
                .build();

        String extractedUsername = jwtTokenValidator.validateAndExtractUser(request);

        assertNull(extractedUsername);
    }

    @Test
    void validateAndExtractUser_WithEmptyCookieValue_ShouldThrowInvalidTokenException() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/some/protected/path")
                .cookie(new HttpCookie(JWT_COOKIE_NAME, ""))
                .build();

        assertThrows(InvalidTokenException.class, () -> {
            jwtTokenValidator.validateAndExtractUser(request);
        });
    }
}