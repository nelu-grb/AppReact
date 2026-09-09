package com.andesstay.mscatalog.controller;

import com.andesstay.mscatalog.dto.UnitRequest;
import com.andesstay.mscatalog.dto.UnitResponse;
import com.andesstay.mscatalog.model.UnitType;
import com.andesstay.mscatalog.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/units")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PostMapping
    public ResponseEntity<UnitResponse> create(@Valid @RequestBody UnitRequest request,
                                                @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogService.create(request, actor(jwt)));
    }

    @GetMapping("/{unitId}")
    public UnitResponse getById(@PathVariable Long unitId) {
        return catalogService.getById(unitId);
    }

    @GetMapping
    public List<UnitResponse> find(@RequestParam(required = false) UnitType type,
                                   @RequestParam(required = false) Boolean availability) {
        return catalogService.find(type, availability);
    }

    @PutMapping("/{unitId}")
    public UnitResponse update(@PathVariable Long unitId,
                               @Valid @RequestBody UnitRequest request,
                               @AuthenticationPrincipal Jwt jwt) {
        return catalogService.update(unitId, request, actor(jwt));
    }

    @DeleteMapping("/{unitId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long unitId, @AuthenticationPrincipal Jwt jwt) {
        catalogService.delete(unitId, actor(jwt));
    }

    private String actor(Jwt jwt) {
        return jwt == null ? "ANONYMOUS" : jwt.getSubject();
    }
}
