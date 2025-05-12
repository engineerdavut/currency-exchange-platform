package com.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException; // io.jsonwebtoken.security.SignatureException

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import com.apigateway.exception.InvalidTokenException;
import com.apigateway.exception.TokenExpiredException;

import javax.crypto.SecretKey;

@Component
public class JwtTokenValidator {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenValidator.class);
    private static final String JWT_COOKIE = "jwt";

    private final SecretKey jwtSecretKey;

    public JwtTokenValidator(SecretKey jwtSecretKey) { // SecretKey, JwtConfig'den @Autowired ile gelecek
        this.jwtSecretKey = jwtSecretKey;
        logger.debug("API Gateway JwtTokenValidator initialized with SecretKey object hash: {}", System.identityHashCode(jwtSecretKey));
    }

    public String validateAndExtractUser(ServerHttpRequest request) {
        HttpCookie cookie = request.getCookies().getFirst(JWT_COOKIE);
        if (cookie == null) {
            logger.debug("No JWT cookie found in request for path: {}", request.getPath());
            return null;
        }

        String token = cookie.getValue();
        if (token == null || token.trim().isEmpty()) {
            logger.warn("JWT cookie found but token value is empty for path: {}", request.getPath());
            // Boş token için InvalidTokenException fırlatmak daha uygun olabilir.
            // Veya bu durumu null döndürerek çağıranın ele almasını sağlayabiliriz.
            // Şimdilik, InvalidTokenException fırlatılmasını bekleyen teste göre davranalım.
            throw new InvalidTokenException("JWT token is empty");
        }

        try {
            Claims claims = Jwts.parser() // parserBuilder() yerine parser() (0.12.x)
                    .verifyWith(jwtSecretKey) // setSigningKey yerine verifyWith
                    .build()
                    .parseSignedClaims(token) // parseClaimsJws yerine parseSignedClaims
                    .getPayload(); // Claims nesnesini almak için .getPayload()

            String username = claims.getSubject();
            logger.debug("Extracted username from JWT: {} for path: {}", username, request.getPath());
            return username;
        } catch (ExpiredJwtException ex) {
            logger.warn("JWT expired for path {}: {}", request.getPath(), ex.getMessage());
            throw new TokenExpiredException("Token expired"); // Kendi özel exception'ımız
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature for path {}: {}", request.getPath(), ex.getMessage());
            throw new InvalidTokenException("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token (malformed) for path {}: {}", request.getPath(), ex.getMessage());
            throw new InvalidTokenException("Invalid JWT token format");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token for path {}: {}", request.getPath(), ex.getMessage());
            throw new InvalidTokenException("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            // Bu genellikle token null veya boş olduğunda veya parse edilemeyen bir karakter içerdiğinde Jwts.parser() tarafından fırlatılır.
            logger.error("JWT claims string is empty or invalid for path {}: {}", request.getPath(), ex.getMessage());
            throw new InvalidTokenException("JWT claims string is invalid");
        } catch (Exception ex) { // Diğer beklenmedik hatalar
            logger.error("Unexpected JWT validation error for path {}: {}", request.getPath(), ex.getMessage(), ex);
            throw new InvalidTokenException("Invalid token due to an unexpected error");
        }
    }
}
