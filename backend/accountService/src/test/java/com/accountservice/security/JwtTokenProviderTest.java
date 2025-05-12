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
    private final long expirationTime = 3600000L; // 1 hour in ms

    // JJWT 0.12.x için kullanılacak imza algoritması
    private SecureDigestAlgorithm<SecretKey, SecretKey> signatureAlgorithm;

    @BeforeEach
    void setUp() {
        String secretString = "testSecretKeyThatIsAtLeast32BytesLongForHS256testtest123"; // Uzunluğundan emin ol
        secretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
        
        // Kullanılacak imza algoritmasını belirle (anahtarın algoritmasına uygun olmalı)
        // Keys.hmacShaKeyFor genellikle "HmacSHA256/384/512" ailesinden bir anahtar üretir.
        // Bu durumda Jwts.SIG.HS256, Jwts.SIG.HS384, veya Jwts.SIG.HS512 kullanılabilir.
        // Anahtarınızın uzunluğuna göre JJWT otomatik seçebilir veya siz belirtebilirsiniz.
        // Örneğin, 32 byte (256 bit) bir secret için HS256 uygundur.
        signatureAlgorithm = Jwts.SIG.HS256; // Veya anahtarınıza uygun olanı seçin

        jwtTokenProvider = new JwtTokenProvider(secretKey);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", expirationTime);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        String username = "testUser";
        String token = jwtTokenProvider.generateToken(username);
        assertNotNull(token);
        // Basit bir format kontrolü, daha detaylı parse edip doğrulamak da mümkün.
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
        // Token'ı geçmiş bir tarihte expire olacak şekilde oluştur
        Date issuedAt = new Date(now.getTime() - 2 * expirationTime); // 2 saat önce issue edilmiş gibi
        Date expiryDate = new Date(now.getTime() - expirationTime);   // 1 saat önce expire olmuş gibi

        String expiredToken = Jwts.builder()
                .subject(username)
                .issuedAt(issuedAt) // Yeni API
                .expiration(expiryDate) // Yeni API
                .signWith(secretKey, signatureAlgorithm) // Yeni API
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
                .signWith(differentKey, signatureAlgorithm) // Farklı anahtarla imzala
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
