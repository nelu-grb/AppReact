package com.andesstay.msreservations.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record AuthenticatedUser(String oid, String name, String email, String actor, Set<String> roles) {

    private static final Set<String> RESERVATION_WIDE_ACCESS_ROLES =
            Set.of("ADMIN", "RECEPTIONIST", "AUDIT");

    public static AuthenticatedUser from(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se encontró al usuario autenticado");
        }

        String name = jwt.getClaimAsString("name");
        String email = findClaim(jwt, "email", "preferred_username", "upn", "unique_name");
        String actor = findClaim(jwt, "preferred_username", "unique_name", "name");
        if (actor == null || actor.isBlank()) actor = jwt.getSubject();

        List<String> tokenRoles = jwt.getClaimAsStringList("roles");
        Set<String> roles = tokenRoles == null
                ? Set.of()
                : tokenRoles.stream()
                        .map(RoleUtils::normalizeRole)
                        .filter(role -> !role.isBlank())
                        .collect(Collectors.toUnmodifiableSet());

        return new AuthenticatedUser(jwt.getClaimAsString("oid"), name, email, actor, roles);
    }

    public String requireOid() {
        return requireClaim(oid, "oid");
    }

    public String requireName() {
        return requireClaim(name, "name");
    }

    public String requireEmail() {
        return requireClaim(email, "email or username");
    }

    public boolean canViewAllReservations() {
        return roles.stream().anyMatch(RESERVATION_WIDE_ACCESS_ROLES::contains);
    }

    private static String findClaim(Jwt jwt, String... claims) {
        for (String claim : claims) {
            String value = jwt.getClaimAsString(claim);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String requireClaim(String value, String claimName) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Al token le falta el dato requerido: " + claimName);
        }
        return value;
    }
}
