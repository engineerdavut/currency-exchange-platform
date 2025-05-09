package com.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired
    public JwtTokenValidator(SecretKey jwtSecretKey) {
        this.jwtSecretKey = jwtSecretKey;
    }
    
    public String validateAndExtractUser(ServerHttpRequest request) {
        HttpCookie cookie = request.getCookies().getFirst(JWT_COOKIE);
        if (cookie == null) {
            logger.debug("No JWT cookie found in request");
            return null;
        }
        
        String token = cookie.getValue();
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
                
            String username = claims.getSubject();
            logger.debug("Extracted username from JWT: {}", username);
            return username;
        }catch (ExpiredJwtException ex) {
            logger.warn("JWT expired: {}", ex.getMessage());
            throw new TokenExpiredException("Token expired");
        } catch (Exception ex) {
            logger.error("JWT validation error: {}", ex.getMessage());
            throw new InvalidTokenException("Invalid token");
        }
    }
}
