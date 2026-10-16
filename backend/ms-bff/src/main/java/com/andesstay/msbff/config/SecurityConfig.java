package com.andesstay.msbff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.audience}") String audience) {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Token audience is not valid for this API", null));
        OAuth2TokenValidator<Jwt> scopeValidator = jwt -> hasRequiredScope(jwt)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Required delegated API scope is missing", null));

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuerUri),
                audienceValidator,
                scopeValidator));
        return decoder;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
            .authorizeExchange(exchange -> exchange
                .pathMatchers(HttpMethod.GET, "/api/users/me").hasAuthority("SCOPE_access_as_user")
                .pathMatchers(HttpMethod.GET, "/api/units/**").hasAuthority("SCOPE_access_as_user")
                .pathMatchers(HttpMethod.POST, "/api/units/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.PUT, "/api/units/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/api/units/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.GET, "/reservations/by-unit/**")
                    .hasAnyRole("ADMIN", "RECEPTIONIST", "AUDIT")
                .pathMatchers(HttpMethod.GET, "/reservations", "/reservations/*")
                    .hasAnyRole("ADMIN", "RECEPTIONIST", "AUDIT", "GUEST", "GUEST_USER", "HUESPED")
                .pathMatchers(HttpMethod.POST, "/reservations/confirm-payment")
                    .hasAnyRole("ADMIN", "RECEPTIONIST", "GUEST", "GUEST_USER", "HUESPED")
                .pathMatchers(HttpMethod.POST, "/reservations/*/pay")
                    .hasAnyRole("ADMIN", "RECEPTIONIST", "GUEST", "GUEST_USER", "HUESPED")
                .pathMatchers(HttpMethod.POST, "/reservations")
                    .hasAnyRole("ADMIN", "RECEPTIONIST", "GUEST", "GUEST_USER", "HUESPED")
                .pathMatchers(HttpMethod.PUT, "/reservations/*/status")
                    .hasAnyRole("ADMIN", "RECEPTIONIST")
                .pathMatchers(HttpMethod.DELETE, "/reservations/*")
                    .hasAnyRole("ADMIN", "RECEPTIONIST")
                .pathMatchers("/audit/**").hasAnyRole("ADMIN", "AUDIT")
                .pathMatchers("/reports/**").hasRole("ADMIN")
                .pathMatchers("/api/deployments/**").hasRole("ADMIN")
                .anyExchange().denyAll()
            );

        return http.build();
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>(scopeConverter.convert(jwt));
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.stream()
                        .map(SecurityConfig::normalizeRole)
                        .map(role -> (GrantedAuthority) () -> "ROLE_" + role)
                        .forEach(authorities::add);
            }
            return authorities;
        });
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    private static String normalizeRole(String role) {
        return Normalizer.normalize(role.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
    }

    private static boolean hasRequiredScope(Jwt jwt) {
        String scopes = jwt.getClaimAsString("scp");
        if (scopes == null) {
            scopes = jwt.getClaimAsString("scope");
        }
        return scopes != null && List.of(scopes.split("\\s+")).contains("access_as_user");
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "https://ashy-stone-0e63b4a0f.6.azurestaticapps.net"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}