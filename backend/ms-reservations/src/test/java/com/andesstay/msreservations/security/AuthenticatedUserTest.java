package com.andesstay.msreservations.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticatedUserTest {

    @Test
    void recognizesOnlyConfiguredReservationWideAccessRoles() {
        AuthenticatedUser receptionist = userWithRole("Receptionist");
        AuthenticatedUser auditor = userWithRole("Audit");
        AuthenticatedUser unknown = userWithRole("Unknown");

        assertTrue(receptionist.canViewAllReservations());
        assertTrue(auditor.canViewAllReservations());
        assertFalse(unknown.canViewAllReservations());
    }

    private AuthenticatedUser userWithRole(String role) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("roles", List.of(role))
                .subject("subject")
                .build();
        return AuthenticatedUser.from(jwt);
    }
}
