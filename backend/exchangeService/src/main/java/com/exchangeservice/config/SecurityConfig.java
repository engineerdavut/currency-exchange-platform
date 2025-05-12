// src/main/java/com/exchangeservice/config/SecurityConfig.java (Exchange Service İÇİNDE)
package com.exchangeservice.config; // Paket adını kontrol et

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // API Gateway arkasında genellikle gereksiz
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll() // Opsiyonel: Actuator endpointleri varsa
                .anyRequest().permitAll() // <<< API Gateway'e güvendiğimiz için TÜM istekleri kabul et
            )
            // Backend servisler genellikle state'sizdir
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
