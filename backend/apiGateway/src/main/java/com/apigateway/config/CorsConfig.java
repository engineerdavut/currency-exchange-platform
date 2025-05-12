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

    @Value("${cors.allowedOrigins}") // .env'den değeri almak için
    private String allowedOriginsFromEnv;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // config.setAllowedOrigins(List.of("${CORS_ALLOWED_ORIGINS}")); // Bu şekilde çalışmaz, String'i ayrıştırmaz
        if (allowedOriginsFromEnv != null && !allowedOriginsFromEnv.isEmpty()) {
            // Eğer birden fazla origin varsa virgülle ayrılmış olabilir, ona göre parse edin
            // Şimdilik tek origin olduğunu varsayıyorum
            config.setAllowedOrigins(List.of(allowedOriginsFromEnv));
        } else {
            // Fallback veya hata fırlatma
            config.setAllowedOrigins(List.of(allowedOriginsFromEnv)); // Güvenli bir varsayılan
            // Veya geliştirme için: config.setAllowedOrigins(List.of("http://localhost:3000"));
        }

        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of(
            "Authorization","Cache-Control","Content-Type","X-User-Name", "X-User", "X-Requested-With" // X-User ve X-Requested-With eklendi
        ));
        config.setExposedHeaders(List.of("X-User", "Set-Cookie", "Authorization")); // Set-Cookie ve Authorization da expose edilmeli
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}