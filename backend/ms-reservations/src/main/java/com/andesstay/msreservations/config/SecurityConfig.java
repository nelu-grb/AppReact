package com.andesstay.msreservations.config;

import com.andesstay.msreservations.security.RoleUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.audience}") String audience) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "El token no corresponde a esta api", null));
        OAuth2TokenValidator<Jwt> scopeValidator = jwt -> hasRequiredScope(jwt)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Al token le falta el permiso delegado requerido", null));

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuerUri),
                audienceValidator,
                scopeValidator));
        return decoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/reservations/by-unit/**")
                    .hasAnyRole("ADMIN", "RECEPTIONIST", "AUDIT")
                .requestMatchers(HttpMethod.GET, "/api/reservations", "/api/reservations/*")
                    .hasAnyRole("ADMIN", "RECEPTIONIST", "AUDIT", "GUEST", "GUEST_USER", "HUESPED")
                .requestMatchers(HttpMethod.POST, "/api/reservations/confirm-payment")
                    .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/reservations/*/pay")
                    .hasAnyRole("ADMIN", "RECEPTIONIST", "GUEST", "GUEST_USER", "HUESPED")
                .requestMatchers(HttpMethod.POST, "/api/reservations")
                    .hasAnyRole("ADMIN", "RECEPTIONIST", "GUEST", "GUEST_USER", "HUESPED")
                .requestMatchers(HttpMethod.PUT, "/api/reservations/*/status")
                    .hasAnyRole("ADMIN", "RECEPTIONIST")
                .requestMatchers(HttpMethod.DELETE, "/api/reservations/*")
                    .hasAnyRole("ADMIN", "RECEPTIONIST")
                .anyRequest().denyAll())
            .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
            .build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>(scopeConverter.convert(jwt));
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.stream()
                        .map(RoleUtils::normalizeRole)
                        .map(role -> (GrantedAuthority) () -> "ROLE_" + role)
                        .forEach(authorities::add);
            }
            return authorities;
        });
        return converter;
    }

    private static boolean hasRequiredScope(Jwt jwt) {
        String scopes = jwt.getClaimAsString("scp");
        if (scopes == null) {
            scopes = jwt.getClaimAsString("scope");
        }
        return scopes != null && List.of(scopes.split("\\s+")).contains("access_as_user");
    }
}