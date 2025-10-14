package com.example.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class CurrentUser {

    public UUID keycloakId(JwtAuthenticationToken auth) {
        var jwt = auth.getToken();
        var sub = Optional.ofNullable(jwt.getClaimAsString("sub")).orElse(auth.getName());
        return UUID.fromString(sub);
    }

    public String username(JwtAuthenticationToken auth) {
        var jwt = auth.getToken();
        return Optional.ofNullable(jwt.getClaimAsString("preferred_username"))
                .or(() -> Optional.ofNullable(jwt.getClaimAsString("email")))
                .orElse(auth.getName());
    }

    public Map<String, Object> claims(JwtAuthenticationToken auth) {
        return auth.getToken().getClaims();
    }
}
