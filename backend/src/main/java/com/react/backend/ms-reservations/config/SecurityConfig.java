package com.andesstay.reservations.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/reservations/**").hasAnyAuthority("APPROLE_Admin", "APPROLE_Operador", "APPROLE_Cliente", "APPROLE_Auditor")
                .requestMatchers(HttpMethod.POST, "/api/reservations").hasAnyAuthority("APPROLE_Cliente", "APPROLE_Operador", "APPROLE_Admin")
                .requestMatchers(HttpMethod.PUT, "/api/reservations/*/status").hasAnyAuthority("APPROLE_Operador", "APPROLE_Admin")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}