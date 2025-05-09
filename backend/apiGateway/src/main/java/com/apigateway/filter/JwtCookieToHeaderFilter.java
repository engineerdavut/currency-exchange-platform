package com.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtCookieToHeaderFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(JwtCookieToHeaderFilter.class);
    
    private final JwtTokenValidator jwtValidator;
    
    @Autowired
    public JwtCookieToHeaderFilter(JwtTokenValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        logger.debug("[JwtCookieToHeader] incoming path={}", path);

        // auth endpoint'lerini atla
        if (path.startsWith("/api/auth/")) {
            return chain.filter(exchange);
        }

        // JWT doğrulama ve username çıkarma
        String username = jwtValidator.validateAndExtractUser(exchange.getRequest());
        
        if (username != null) {
            logger.debug("[JwtCookieToHeader] Adding X-User header: {}", username);
            
            return chain.filter(exchange.mutate().request(
                exchange.getRequest().mutate()
                    .header("X-User", username)
                    .build()
            ).build());
        }
        
        // Korumalı yol ve geçerli token yoksa 401 dön
        if (isProtectedPath(path)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        logger.debug("[JwtCookieToHeader] no JWT cookie present");
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
    
    private boolean isProtectedPath(String path) {
        return !path.startsWith("/api/auth/") 
            && !path.contains("/actuator")
            && !path.contains("/public");
    }
}

