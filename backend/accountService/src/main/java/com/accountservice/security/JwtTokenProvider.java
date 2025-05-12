package com.accountservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException; // Specific exception
import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm; // DEPRECATED, kaldırıldı
import io.jsonwebtoken.UnsupportedJwtException; // Specific exception
import io.jsonwebtoken.MalformedJwtException; // Specific exception// Eğer anahtarı dinamik oluşturuyorsanız (örneğin testlerde)
import io.jsonwebtoken.security.SignatureException; // Specific exception
import io.jsonwebtoken.security.SecureDigestAlgorithm; // Yeni imza algoritması arayüzü

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey key; // Bu SecretKey'in HMAC SHA algoritmaları için uygun olması gerekir

    @Value("${jwt.expiration}")
    private long jwtExpirationMs; // Milisaniye cinsinden olduğunu varsayıyorum

    public JwtTokenProvider(SecretKey key) {
        this.key = key;
        logger.debug("AccountService JwtTokenProvider initialized with SecretKey object hash: {}", System.identityHashCode(key));
        // Anahtarın algoritmasını ve formatını loglamak da faydalı olabilir
        // logger.debug("SecretKey Algorithm: {}, Format: {}", key.getAlgorithm(), key.getFormat());
    }

    public String generateToken(String username) {
        logger.debug("[JwtTokenProvider] generateToken: username={}", username);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // JJWT 0.12.x ile uyumlu builder ve imzalama
        // SecureDigestAlgorithm'ı SecretKey'e uygun olarak seçmeliyiz.
        // SecretKey'iniz Keys.hmacShaKeyFor ile oluşturulduysa, HS256, HS384, HS512 uygundur.
        // Varsayılan olarak anahtarınızın HS256 için uygun olduğunu varsayalım.
        SecureDigestAlgorithm<SecretKey, SecretKey> signatureAlgorithm = Jwts.SIG.HS256; // VEYA Jwts.SIG.get().get(key.getAlgorithm())
                                                                                       // VEYA anahtarınızın oluşturulma şekline göre doğru algoritma

        // Eğer anahtarınızın algoritmasını biliyorsanız (örn: "HmacSHA256") ve buna uygun bir SecureDigestAlgorithm almak istiyorsanız:
        // SecureDigestAlgorithm<?, ?> signatureAlgorithm = Jwts.SIG.get().forKey(key);
        // Veya spesifik olarak:
        // if (! (key instanceof javax.crypto.spec.SecretKeySpec && key.getAlgorithm().startsWith("HmacSHA"))) {
        //     logger.warn("The provided key is not an HMAC SHA key, default HS256 might not be appropriate.");
        // }


        String token = Jwts.builder()
                .subject(username) // Deprecated olmayan metot
                .issuedAt(now)     // Deprecated olmayan metot
                .expiration(expiryDate) // Deprecated olmayan metot
                .signWith(key, signatureAlgorithm) // Deprecated olmayan imzalama metodu
                .compact();

        String tokenStart = token.substring(0, Math.min(token.length(), 15));
        logger.debug("Generated JWT for user '{}', starts with: {}...", username, tokenStart);

        return token;
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser() // parserBuilder() yerine parser() kullanılabilir (0.12.x'te)
                    .verifyWith(key) // setSigningKey yerine verifyWith (daha güvenli)
                    .build()
                    .parseSignedClaims(token) // parseClaimsJws yerine parseSignedClaims
                    .getPayload();
            
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
            throw e; // Veya özel bir exception
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            throw e; // Veya özel bir exception
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
        } catch (Exception e) { // Diğer beklenmedik hatalar için genel bir catch
            logger.error("[JwtTokenProvider] validateToken: invalid due to an unexpected error", e);
        }
        return false;
    }
}
