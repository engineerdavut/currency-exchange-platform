package com.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowedOrigins}")
    private String allowedOriginsFromEnv;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
       
        if (allowedOriginsFromEnv != null && !allowedOriginsFromEnv.isEmpty()) {

            config.setAllowedOrigins(List.of(allowedOriginsFromEnv));
        } else {
            config.setAllowedOrigins(List.of(allowedOriginsFromEnv));
        }

        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of(
            "Authorization","Cache-Control","Content-Type","X-User-Name", "X-User", "X-Requested-With" 
        ));
        config.setExposedHeaders(List.of("X-User", "Set-Cookie", "Authorization")); 
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}