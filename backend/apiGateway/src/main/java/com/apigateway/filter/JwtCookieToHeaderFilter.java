package com.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException; 

@Component
public class JwtCookieToHeaderFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(JwtCookieToHeaderFilter.class);
    
    private final JwtTokenValidator jwtValidator;
    
    public JwtCookieToHeaderFilter(JwtTokenValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        logger.debug("[JwtCookieToHeader] incoming path={}", path);

        if (path.startsWith("/api/auth/")) {
            return chain.filter(exchange);
        }

        String username = jwtValidator.validateAndExtractUser(exchange.getRequest());
        
        if (username != null) {
            try {
                String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8.toString());
                logger.debug("[JwtCookieToHeader] Original username: {}, Encoded username for X-User header: {}", username, encodedUsername);

                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User", encodedUsername)
                        .build();
                
                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (UnsupportedEncodingException e) {
                logger.error("[JwtCookieToHeader] Error URL encoding username: {}", username, e);
                return chain.filter(exchange); 
            }
        }
        
        if (isProtectedPath(path)) {
            logger.warn("[JwtCookieToHeader] Protected path {} accessed without valid JWT. Responding with UNAUTHORIZED.", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        logger.debug("[JwtCookieToHeader] No JWT cookie present for path: {}", path);
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE+10;
    }
    
    private boolean isProtectedPath(String path) {
        return !path.startsWith("/api/auth/") 
            && !path.contains("/actuator")
            && !path.contains("/public");
    }
}

