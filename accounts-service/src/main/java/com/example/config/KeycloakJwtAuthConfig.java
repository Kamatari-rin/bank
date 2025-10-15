package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class KeycloakJwtAuthConfig {

    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> keycloakResourceRolesConverter() {
        return jwt -> {
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess == null) return List.of();

            Map<String, Object> acc = (Map<String, Object>) resourceAccess.getOrDefault("accounts-service", Map.of());
            List<String> roles = (List<String>) acc.getOrDefault("roles", List.of());
            return roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toSet());
        };
    }
}