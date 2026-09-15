package com.andesstay.msbff.controller;

import com.andesstay.msbff.dto.UserResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public Mono<UserResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        UserResponse user = new UserResponse();
        user.setId(jwt.getSubject());
        user.setName(jwt.getClaimAsString("name"));
        user.setEmail(jwt.getClaimAsString("email"));
        user.setPreferredUsername(jwt.getClaimAsString("preferred_username"));
        
        return Mono.just(user);
    }
}
