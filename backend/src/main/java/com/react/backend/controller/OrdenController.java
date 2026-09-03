package com.react.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_OT.create')")
    public Map<String, Object> crearOrden(
            @AuthenticationPrincipal Jwt jwt, 
            @RequestBody(required = false) Map<String, Object> body) {
        return Map.of(
            "status", "OK",
            "mensaje", "Orden de trabajo creada exitosamente en el backend",
            "sub", jwt.getSubject(),
            "scope", jwt.getClaimAsString("scp")
        );
    }
}